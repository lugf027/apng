package io.github.lugf027.apng

import okio.ByteString.Companion.encodeUtf8
import okio.Path
import okio.use

public class DiskCacheStrategy(
    private val diskCache: DiskCache = SharedDiskCache
) : ApngCacheStrategy {

    override fun path(url: String): Path? {
        return try {
            diskCache.openSnapshot(key(url)).use { it?.data }
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun save(url: String, bytes: ByteArray): Path? {
        val editor = diskCache.openEditor(key(url)) ?: return null

        return try {
            diskCache.fileSystem.write(editor.data) {
                write(bytes)
            }
            editor.commitAndOpenSnapshot().use { it?.data }
        } catch (_: Throwable) {
            editor.abort()
            null
        }
    }

    override suspend fun load(url: String): ByteArray? {
        val snapshot = diskCache.openSnapshot(key(url)) ?: return null

        snapshot.use {
            return diskCache.fileSystem.read(it.data) {
                readByteArray()
            }
        }
    }

    override suspend fun clear() {
        diskCache.clear()
    }

    private fun key(url: String) = url.encodeUtf8().sha256().hex()

    @InternalApngApi
    public companion object {
        public val Instance: DiskCacheStrategy by lazy {
            DiskCacheStrategy()
        }
    }
}
