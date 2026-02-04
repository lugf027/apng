package io.github.lugf027.apng.network

import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger
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
            val path = diskCache.put(url.sha256(), bytes)
            ApngLogger.v(ApngLogTags.CACHE) { "DiskApngCacheStrategy: Saved ${bytes.size} bytes to $path" }
            path
        } catch (e: Exception) {
            ApngLogger.w(ApngLogTags.CACHE, "DiskApngCacheStrategy: Failed to save to cache: ${e.message}", e)
            null
        }
    }

    override suspend fun load(url: String): ByteArray? {
        return try {
            val bytes = diskCache.get(url.sha256())
            if (bytes != null) {
                ApngLogger.v(ApngLogTags.CACHE) { "DiskApngCacheStrategy: Loaded ${bytes.size} bytes from cache" }
            }
            bytes
        } catch (e: Exception) {
            ApngLogger.w(ApngLogTags.CACHE, "DiskApngCacheStrategy: Failed to load from cache: ${e.message}", e)
            null
        }
    }

    override suspend fun clear() {
        ApngLogger.d(ApngLogTags.CACHE, "DiskApngCacheStrategy: Clearing cache")
        diskCache.clear()
        ApngLogger.i(ApngLogTags.CACHE, "DiskApngCacheStrategy: Cache cleared")
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
                ApngLogger.d(ApngLogTags.CACHE) { "DiskApngCacheStrategy: Creating instance with cacheDir=$directory, maxSize=$maxSize" }
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
                ApngLogger.d(ApngLogTags.CACHE) { "DiskApngCacheStrategy: Lazy creating instance with default cacheDir=$directory" }
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
