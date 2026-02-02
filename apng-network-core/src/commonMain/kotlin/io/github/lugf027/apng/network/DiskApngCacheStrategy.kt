package io.github.lugf027.apng.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path

/**
 * Disk-based LRU cache strategy for APNG files.
 * Caches downloaded APNG files to disk with automatic LRU eviction.
 */
class DiskApngCacheStrategy internal constructor(
    private val diskCache: DiskLruCache
) : ApngCacheStrategy {
    
    override fun path(url: String): Path? {
        // Note: This is a synchronous check, may not be perfectly accurate
        // but provides a quick path lookup without suspension
        return diskCache.directory / url.sha256()
    }

    override suspend fun save(url: String, bytes: ByteArray): Path? {
        return try {
            diskCache.put(url.sha256(), bytes)
        } catch (e: Exception) {
            println("Failed to save APNG to cache: ${e.message}")
            null
        }
    }

    override suspend fun load(url: String): ByteArray? {
        return try {
            diskCache.get(url.sha256())
        } catch (e: Exception) {
            println("Failed to load APNG from cache: ${e.message}")
            null
        }
    }

    override suspend fun clear() {
        diskCache.clear()
    }

    companion object {
        private val mutex = Mutex()
        private var instance: DiskApngCacheStrategy? = null

        /**
         * Get or create the default DiskApngCacheStrategy instance.
         * @param cacheDir Optional custom cache directory path
         * @param maxSize Optional maximum cache size in bytes (default 100MB)
         */
        suspend fun getInstance(
            cacheDir: Path? = null,
            maxSize: Long = 100L * 1024L * 1024L
        ): DiskApngCacheStrategy = mutex.withLock {
            instance ?: run {
                val directory = cacheDir ?: getDefaultCacheDirectory()
                val diskCache = DiskLruCache(directory, maxSize)
                DiskApngCacheStrategy(diskCache).also { instance = it }
            }
        }

        /**
         * Get the default DiskApngCacheStrategy instance.
         * Creates one lazily if not exists.
         */
        val Instance: DiskApngCacheStrategy
            get() {
                // Fast path: return existing instance
                instance?.let { return it }
                
                // Slow path: create new instance (not perfectly thread-safe but acceptable for lazy init)
                val directory = getDefaultCacheDirectory()
                val diskCache = DiskLruCache(directory)
                return DiskApngCacheStrategy(diskCache).also { instance = it }
            }
    }
}

/**
 * Get the default cache directory for APNG files.
 * Platform-specific implementation.
 */
internal expect fun getDefaultCacheDirectory(): Path
