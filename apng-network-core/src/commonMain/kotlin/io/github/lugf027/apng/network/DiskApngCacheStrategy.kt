package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Disk-based LRU cache strategy for APNG files.
 * Caches downloaded APNG files to disk with automatic LRU eviction.
 */
class DiskApngCacheStrategy(
    private val diskCache: DiskLruCache
) : ApngCacheStrategy {
    override fun path(url: String): Path? {
        val key = url.sha256()
        return if (diskCache.contains(key)) {
            diskCache.directory / key
        } else {
            null
        }
    }

    override suspend fun save(url: String, bytes: ByteArray): Path? {
        return try {
            diskCache.put(url.sha256(), bytes)
        } catch (e: Exception) {
            System.err.println("Failed to save APNG to cache: ${e.message}")
            null
        }
    }

    override suspend fun load(url: String): ByteArray? {
        return try {
            diskCache.get(url.sha256())
        } catch (e: Exception) {
            System.err.println("Failed to load APNG from cache: ${e.message}")
            null
        }
    }

    override suspend fun clear() {
        diskCache.clear()
    }

    companion object {
        @Volatile
        private var instance: DiskApngCacheStrategy? = null

        /**
         * Get or create the default DiskApngCacheStrategy instance.
         */
        val Instance: DiskApngCacheStrategy
            get() {
                if (instance == null) {
                    synchronized(this) {
                        if (instance == null) {
                            val cacheDir = getCacheDirectory()
                            val diskCache = DiskLruCache(cacheDir)
                            instance = DiskApngCacheStrategy(diskCache)
                        }
                    }
                }
                return instance!!
            }

        /**
         * Get the system cache directory for APNG files.
         */
        private fun getCacheDirectory(): Path {
            val tmpDir = System.getProperty("java.io.tmpdir") ?: "/tmp"
            return "$tmpDir/apng-cache".toPath()
        }
    }
}
