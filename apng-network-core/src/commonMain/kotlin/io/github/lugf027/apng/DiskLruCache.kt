package io.github.lugf027.apng

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okio.BufferedSink
import okio.Closeable
import okio.EOFException
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Sink
import okio.blackholeSink
import okio.buffer
import kotlin.coroutines.CoroutineContext

internal class DiskLruCache(
    fileSystem: FileSystem,
    private val directory: Path,
    cleanupContext: CoroutineContext,
    private val maxSize: Long,
    private val appVersion: Int,
    private val valueCount: Int,
) : Closeable {

    init {
        require(maxSize > 0L) { "maxSize <= 0" }
        require(valueCount > 0) { "valueCount <= 0" }
    }

    private val journalFile = directory / JOURNAL_FILE
    private val journalFileTmp = directory / JOURNAL_FILE_TMP
    private val journalFileBackup = directory / JOURNAL_FILE_BACKUP
    private val lruEntries = LinkedHashMap<String, Entry>()

    @OptIn(ExperimentalStdlibApi::class, InternalApngApi::class)
    private val cleanupScope = CoroutineScope(
        cleanupContext +
            SupervisorJob() +
            (cleanupContext[CoroutineDispatcher] ?: Apng.ioDispatcher())
                .limitedParallelism(1)
    )
    private val lock = SynchronizedObject()
    private var size = 0L
    private var operationsSinceRewrite = 0
    private var journalWriter: BufferedSink? = null
    private var hasJournalErrors = false
    private var initialized = false
    private var closed = false
    private var mostRecentTrimFailed = false
    private var mostRecentRebuildFailed = false

    private val fileSystem = object : ForwardingFileSystem(fileSystem) {
        override fun sink(file: Path, mustCreate: Boolean): Sink {
            file.parent?.let(::createDirectories)
            return super.sink(file, mustCreate)
        }
    }

    private fun initialize() {
        if (!initialized) {
            fileSystem.delete(journalFileTmp)

            if (fileSystem.exists(journalFileBackup)) {
                if (fileSystem.exists(journalFile)) {
                    fileSystem.delete(journalFileBackup)
                } else {
                    fileSystem.atomicMove(journalFileBackup, journalFile)
                }
            }

            if (fileSystem.exists(journalFile)) {
                try {
                    readJournal()
                    processJournal()
                    initialized = true
                    return
                } catch (_: IOException) {
                }

                try {
                    delete()
                } finally {
                    closed = false
                }
            }

            writeJournal()
            initialized = true
        }
    }

    private fun readJournal() {
        fileSystem.read(journalFile) {
            val magic = readUtf8LineStrict()
            val version = readUtf8LineStrict()
            val appVersionString = readUtf8LineStrict()
            val valueCountString = readUtf8LineStrict()
            val blank = readUtf8LineStrict()

            if (MAGIC != magic ||
                VERSION != version ||
                appVersion.toString() != appVersionString ||
                valueCount.toString() != valueCountString ||
                blank.isNotEmpty()
            ) {
                throw IOException(
                    "unexpected journal header: " +
                        "[$magic, $version, $appVersionString, $valueCountString, $blank]"
                )
            }

            var lineCount = 0
            while (true) {
                try {
                    readJournalLine(readUtf8LineStrict())
                    lineCount++
                } catch (_: EOFException) {
                    break
                }
            }

            operationsSinceRewrite = lineCount - lruEntries.size

            if (!exhausted()) {
                writeJournal()
            } else {
                journalWriter = newJournalWriter()
            }
        }
    }

    private fun newJournalWriter(): BufferedSink {
        val fileSink = fileSystem.appendingSink(journalFile)
        val faultHidingSink = FaultHidingSink(fileSink) {
            hasJournalErrors = true
        }
        return faultHidingSink.buffer()
    }

    private fun readJournalLine(line: String) {
        val firstSpace = line.indexOf(' ')
        if (firstSpace == -1) throw IOException("unexpected journal line: $line")

        val keyBegin = firstSpace + 1
        val secondSpace = line.indexOf(' ', keyBegin)
        val key: String
        if (secondSpace == -1) {
            key = line.substring(keyBegin)
            if (firstSpace == REMOVE.length && line.startsWith(REMOVE)) {
                lruEntries.remove(key)
                return
            }
        } else {
            key = line.substring(keyBegin, secondSpace)
        }

        val entry = lruEntries.getOrPut(key) { Entry(key) }
        when {
            secondSpace != -1 && firstSpace == CLEAN.length && line.startsWith(CLEAN) -> {
                val parts = line.substring(secondSpace + 1).split(' ')
                entry.readable = true
                entry.currentEditor = null
                entry.setLengths(parts)
            }
            secondSpace == -1 && firstSpace == DIRTY.length && line.startsWith(DIRTY) -> {
                entry.currentEditor = Editor(entry)
            }
            secondSpace == -1 && firstSpace == READ.length && line.startsWith(READ) -> {
                // Already done by getOrPut
            }
            else -> throw IOException("unexpected journal line: $line")
        }
    }

    private fun processJournal() {
        var size = 0L
        val iterator = lruEntries.values.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.currentEditor == null) {
                for (i in 0 until valueCount) {
                    size += entry.lengths[i]
                }
            } else {
                entry.currentEditor = null
                for (i in 0 until valueCount) {
                    fileSystem.delete(entry.cleanFiles[i])
                    fileSystem.delete(entry.dirtyFiles[i])
                }
                iterator.remove()
            }
        }
        this.size = size
    }

    private fun writeJournal() = synchronized(lock) {
        journalWriter?.close()

        fileSystem.write(journalFileTmp) {
            writeUtf8(MAGIC).writeByte('\n'.code)
            writeUtf8(VERSION).writeByte('\n'.code)
            writeDecimalLong(appVersion.toLong()).writeByte('\n'.code)
            writeDecimalLong(valueCount.toLong()).writeByte('\n'.code)
            writeByte('\n'.code)

            for (entry in lruEntries.values) {
                if (entry.currentEditor != null) {
                    writeUtf8(DIRTY).writeByte(' '.code).writeUtf8(entry.key).writeByte('\n'.code)
                } else {
                    writeUtf8(CLEAN).writeByte(' '.code).writeUtf8(entry.key)
                    entry.writeLengths(this)
                    writeByte('\n'.code)
                }
            }
        }

        if (fileSystem.exists(journalFile)) {
            fileSystem.atomicMove(journalFile, journalFileBackup)
            fileSystem.atomicMove(journalFileTmp, journalFile)
            fileSystem.delete(journalFileBackup)
        } else {
            fileSystem.atomicMove(journalFileTmp, journalFile)
        }

        journalWriter = newJournalWriter()
        operationsSinceRewrite = 0
        hasJournalErrors = false
        mostRecentRebuildFailed = false
    }

    operator fun get(key: String): Snapshot? = synchronized(lock) {
        checkNotClosed()
        validateKey(key)
        initialize()

        val snapshot = lruEntries[key]?.snapshot() ?: return null

        operationsSinceRewrite++
        journalWriter!!.apply {
            writeUtf8(READ).writeByte(' '.code).writeUtf8(key).writeByte('\n'.code)
            flush()
        }

        if (journalRewriteRequired()) launchCleanup()

        return snapshot
    }

    fun edit(key: String): Editor? = synchronized(lock) {
        checkNotClosed()
        validateKey(key)
        initialize()

        var entry = lruEntries[key]

        if (entry?.currentEditor != null) return null
        if (entry != null && entry.lockingSnapshotCount != 0) return null

        if (mostRecentTrimFailed || mostRecentRebuildFailed) {
            launchCleanup()
            return null
        }

        journalWriter!!.apply {
            writeUtf8(DIRTY).writeByte(' '.code).writeUtf8(key).writeByte('\n'.code)
            flush()
        }

        if (hasJournalErrors) return null

        if (entry == null) {
            entry = Entry(key)
            lruEntries[key] = entry
        }
        val editor = Editor(entry)
        entry.currentEditor = editor
        return editor
    }

    fun size(): Long = synchronized(lock) {
        initialize()
        return size
    }

    @OptIn(InternalApngApi::class)
    private fun completeEdit(editor: Editor, success: Boolean) = synchronized(lock) {
        val entry = editor.entry
        check(entry.currentEditor == editor)

        if (success && !entry.zombie) {
            for (i in 0 until valueCount) {
                if (editor.written[i] && !fileSystem.exists(entry.dirtyFiles[i])) {
                    editor.abort()
                    return
                }
            }
            for (i in 0 until valueCount) {
                val dirty = entry.dirtyFiles[i]
                val clean = entry.cleanFiles[i]
                if (fileSystem.exists(dirty)) {
                    fileSystem.atomicMove(dirty, clean)
                } else {
                    fileSystem.createFile(entry.cleanFiles[i])
                }
                val oldLength = entry.lengths[i]
                val newLength = fileSystem.metadata(clean).size ?: 0
                entry.lengths[i] = newLength
                size = size - oldLength + newLength
            }
        } else {
            for (i in 0 until valueCount) {
                fileSystem.delete(entry.dirtyFiles[i])
            }
        }

        entry.currentEditor = null
        if (entry.zombie) {
            removeEntry(entry)
            return
        }

        operationsSinceRewrite++
        journalWriter!!.apply {
            if (success || entry.readable) {
                entry.readable = true
                writeUtf8(CLEAN).writeByte(' '.code).writeUtf8(entry.key)
                entry.writeLengths(this)
                writeByte('\n'.code)
            } else {
                lruEntries.remove(entry.key)
                writeUtf8(REMOVE).writeByte(' '.code).writeUtf8(entry.key).writeByte('\n'.code)
            }
            flush()
        }

        if (size > maxSize || journalRewriteRequired()) launchCleanup()
    }

    private fun journalRewriteRequired() = operationsSinceRewrite >= 2000

    fun remove(key: String): Boolean = synchronized(lock) {
        checkNotClosed()
        validateKey(key)
        initialize()

        val entry = lruEntries[key] ?: return false
        val removed = removeEntry(entry)
        if (removed && size <= maxSize) mostRecentTrimFailed = false
        return removed
    }

    private fun removeEntry(entry: Entry): Boolean {
        if (entry.lockingSnapshotCount > 0) {
            journalWriter?.apply {
                writeUtf8(DIRTY).writeByte(' '.code).writeUtf8(entry.key).writeByte('\n'.code)
                flush()
            }
        }
        if (entry.lockingSnapshotCount > 0 || entry.currentEditor != null) {
            entry.zombie = true
            return true
        }

        for (i in 0 until valueCount) {
            fileSystem.delete(entry.cleanFiles[i])
            size -= entry.lengths[i]
            entry.lengths[i] = 0
        }

        operationsSinceRewrite++
        journalWriter?.apply {
            writeUtf8(REMOVE).writeByte(' '.code).writeUtf8(entry.key).writeByte('\n'.code)
            flush()
        }
        lruEntries.remove(entry.key)

        if (journalRewriteRequired()) launchCleanup()

        return true
    }

    private fun checkNotClosed() {
        check(!closed) { "cache is closed" }
    }

    override fun close() = synchronized(lock) {
        if (!initialized || closed) {
            closed = true
            return
        }

        for (entry in lruEntries.values.toTypedArray()) {
            entry.currentEditor?.detach()
        }

        trimToSize()
        cleanupScope.cancel()
        journalWriter!!.close()
        journalWriter = null
        closed = true
    }

    fun flush() = synchronized(lock) {
        if (!initialized) return
        checkNotClosed()
        trimToSize()
        journalWriter!!.flush()
    }

    private fun trimToSize() {
        while (size > maxSize) {
            if (!removeOldestEntry()) return
        }
        mostRecentTrimFailed = false
    }

    private fun removeOldestEntry(): Boolean {
        for (toEvict in lruEntries.values) {
            if (!toEvict.zombie) {
                removeEntry(toEvict)
                return true
            }
        }
        return false
    }

    @OptIn(InternalApngApi::class)
    private fun delete() {
        close()
        fileSystem.deleteContents(directory)
    }

    fun evictAll() = synchronized(lock) {
        initialize()
        for (entry in lruEntries.values.toTypedArray()) {
            removeEntry(entry)
        }
        mostRecentTrimFailed = false
    }

    private fun launchCleanup() {
        cleanupScope.launch {
            synchronized(lock) {
                if (!initialized || closed) return@launch
                try {
                    trimToSize()
                } catch (_: IOException) {
                    mostRecentTrimFailed = true
                }
                try {
                    if (journalRewriteRequired()) writeJournal()
                } catch (_: IOException) {
                    mostRecentRebuildFailed = true
                    journalWriter = blackholeSink().buffer()
                }
            }
        }
    }

    private fun validateKey(key: String) {
        require(LEGAL_KEY_PATTERN matches key) {
            "keys must match regex [a-z0-9_-]{1,120}: \"$key\""
        }
    }

    inner class Snapshot(val entry: Entry) : Closeable {
        private var closed = false

        fun file(index: Int): Path {
            check(!closed) { "snapshot is closed" }
            return entry.cleanFiles[index]
        }

        override fun close() {
            if (!closed) {
                closed = true
                synchronized(lock) {
                    entry.lockingSnapshotCount--
                    if (entry.lockingSnapshotCount == 0 && entry.zombie) {
                        removeEntry(entry)
                    }
                }
            }
        }
    }

    inner class Editor(val entry: Entry) {
        private var closed = false
        val written = BooleanArray(valueCount)

        @OptIn(InternalApngApi::class)
        fun file(index: Int): Path {
            synchronized(lock) {
                check(!closed) { "editor is closed" }
                written[index] = true
                return entry.dirtyFiles[index].also(fileSystem::createFile)
            }
        }

        fun detach() {
            if (entry.currentEditor == this) entry.zombie = true
        }

        fun commit() = complete(true)

        fun commitAndGet(): Snapshot? {
            synchronized(lock) {
                commit()
                return get(entry.key)
            }
        }

        fun abort() = complete(false)

        private fun complete(success: Boolean) {
            synchronized(lock) {
                check(!closed) { "editor is closed" }
                if (entry.currentEditor == this) completeEdit(this, success)
                closed = true
            }
        }
    }

    inner class Entry(val key: String) {
        val lengths = LongArray(valueCount)
        val cleanFiles = ArrayList<Path>(valueCount)
        val dirtyFiles = ArrayList<Path>(valueCount)
        var readable = false
        var zombie = false
        var currentEditor: Editor? = null
        var lockingSnapshotCount = 0

        init {
            val fileBuilder = StringBuilder(key).append('.')
            val truncateTo = fileBuilder.length
            for (i in 0 until valueCount) {
                fileBuilder.append(i)
                cleanFiles += directory / fileBuilder.toString()
                fileBuilder.append(".tmp")
                dirtyFiles += directory / fileBuilder.toString()
                fileBuilder.setLength(truncateTo)
            }
        }

        fun setLengths(strings: List<String>) {
            if (strings.size != valueCount) throw IOException("unexpected journal line: $strings")
            try {
                for (i in strings.indices) lengths[i] = strings[i].toLong()
            } catch (_: NumberFormatException) {
                throw IOException("unexpected journal line: $strings")
            }
        }

        fun writeLengths(writer: BufferedSink) {
            for (length in lengths) writer.writeByte(' '.code).writeDecimalLong(length)
        }

        fun snapshot(): Snapshot? {
            if (!readable) return null
            if (currentEditor != null || zombie) return null

            cleanFiles.forEach { file ->
                if (!fileSystem.exists(file)) {
                    try { removeEntry(this) } catch (_: IOException) {}
                    return null
                }
            }
            lockingSnapshotCount++
            return Snapshot(this)
        }
    }

    companion object {
        internal const val JOURNAL_FILE = "journal"
        internal const val JOURNAL_FILE_TMP = "journal.tmp"
        internal const val JOURNAL_FILE_BACKUP = "journal.bkp"
        internal const val MAGIC = "libcore.io.DiskLruCache"
        internal const val VERSION = "1"
        private const val CLEAN = "CLEAN"
        private const val DIRTY = "DIRTY"
        private const val REMOVE = "REMOVE"
        private const val READ = "READ"
        private val LEGAL_KEY_PATTERN = "[a-z0-9_-]{1,120}".toRegex()
    }
}
