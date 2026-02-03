package io.github.lugf027.apng.network

import okio.Buffer
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source

/**
 * Get the default cache directory for web platform.
 * Note: Web doesn't have filesystem access, this is a placeholder.
 */
internal actual fun getDefaultCacheDirectory(): Path {
    // Web platform doesn't support disk caching
    return "/tmp/apng-cache".toPath()
}

/**
 * Simple in-memory file system for web platform.
 * This avoids the kotlinx-datetime dependency issue with FakeFileSystem.
 */
private class InMemoryFileSystem : FileSystem() {
    private val files = mutableMapOf<Path, ByteArray>()
    private val directories = mutableSetOf<Path>()

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        val existing = files[file] ?: ByteArray(0)
        return object : Sink {
            private val buffer = Buffer()
            override fun write(source: Buffer, byteCount: Long) {
                buffer.write(source, byteCount)
            }
            override fun flush() {
                files[file] = existing + buffer.readByteArray()
            }
            override fun timeout() = okio.Timeout.NONE
            override fun close() { flush() }
        }
    }

    override fun atomicMove(source: Path, target: Path) {
        files[source]?.let {
            files[target] = it
            files.remove(source)
        }
    }

    override fun canonicalize(path: Path): Path = path

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        directories.add(dir)
    }

    override fun createSymlink(source: Path, target: Path) {
        // Not supported for web
    }

    override fun delete(path: Path, mustExist: Boolean) {
        files.remove(path)
        directories.remove(path)
    }

    override fun list(dir: Path): List<Path> {
        return files.keys.filter { it.parent == dir } + 
               directories.filter { it.parent == dir }
    }

    override fun listOrNull(dir: Path): List<Path>? {
        return if (directories.contains(dir) || dir == "/".toPath()) {
            list(dir)
        } else null
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        return when {
            files.containsKey(path) -> FileMetadata(
                isRegularFile = true,
                isDirectory = false,
                size = files[path]?.size?.toLong()
            )
            directories.contains(path) -> FileMetadata(
                isRegularFile = false,
                isDirectory = true
            )
            else -> null
        }
    }

    override fun openReadOnly(file: Path): FileHandle {
        throw UnsupportedOperationException("FileHandle not supported in web")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        throw UnsupportedOperationException("FileHandle not supported in web")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        return object : Sink {
            private val buffer = Buffer()
            override fun write(source: Buffer, byteCount: Long) {
                buffer.write(source, byteCount)
            }
            override fun flush() {
                files[file] = buffer.readByteArray()
            }
            override fun timeout() = okio.Timeout.NONE
            override fun close() { flush() }
        }
    }

    override fun source(file: Path): Source {
        val data = files[file] ?: throw okio.FileNotFoundException("File not found: $file")
        return Buffer().apply { write(data) }
    }
}

/**
 * Web platform uses InMemoryFileSystem for in-memory caching.
 * This provides a compatible API while storing data in memory.
 */
private val webFileSystem = InMemoryFileSystem()

/**
 * Get the file system for web platform.
 * Returns an InMemoryFileSystem for in-memory operations.
 */
internal actual fun getSystemFileSystem(): FileSystem = webFileSystem
