package io.github.lugf027.apng

import okio.Closeable
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.coroutines.CoroutineContext
import kotlin.js.JsName

public interface DiskCache {

    public val size: Long
    public val maxSize: Long
    public val directory: Path
    public val fileSystem: FileSystem

    public fun openSnapshot(key: String): Snapshot?
    public fun openEditor(key: String): Editor?
    public fun remove(key: String): Boolean
    public fun clear()
    public fun shutdown()

    public interface Snapshot : Closeable {
        public val metadata: Path
        public val data: Path
        override fun close()
    }

    public interface Editor {
        public val metadata: Path
        public val data: Path
        public fun commit()
        public fun commitAndOpenSnapshot(): Snapshot?
        public fun abort()
    }
}

internal val SharedDiskCache by lazy {
    DiskCache()
}

@OptIn(InternalApngApi::class)
@JsName("ApngDiskCache")
public fun DiskCache(
    directory: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("apng_disk_cache".toPath()),
    fileSystem: FileSystem = defaultFileSystem(),
    maxSizeBytes: Long = MB_250,
    cleanupContext: CoroutineContext = Apng.ioDispatcher()
): DiskCache = RealDiskCache(
    maxSize = maxSizeBytes,
    directory = directory,
    fileSystem = fileSystem,
    cleanupContext = cleanupContext
)

private const val MB_250 = 250L * 1024 * 1024
