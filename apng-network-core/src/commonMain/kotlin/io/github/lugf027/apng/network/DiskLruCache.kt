package io.github.lugf027.apng.network

import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.ByteString.Companion.encodeUtf8
import okio.FileNotFoundException
import okio.FileSystem
import okio.Path

/**
 * A simple LRU disk cache implementation using Okio.
 * Based on the design of compottie's DiskLruCache.
 * Uses coroutine Mutex for thread-safety across all platforms.
 *
 * @param directory The directory to store cache files
 * @param maxSize The maximum cache size in bytes
 * @param fileSystem The file system to use (platform-specific)
 */
internal class DiskLruCache(
    val directory: Path,
    val maxSize: Long = 100L * 1024L * 1024L, // 100 MB default
    private val fileSystem: FileSystem = getSystemFileSystem()
) {
    private val mutex = Mutex()
    private val accessTimes = mutableMapOf<String, Long>()
    private var currentSize = 0L
    private var accessCounter = 0L

    init {
        try {
            fileSystem.createDirectories(directory, mustCreate = false)
            ApngLogger.d(ApngLogTags.CACHE) { "DiskLruCache: Initialized with directory=$directory, maxSize=$maxSize bytes" }
        } catch (e: Exception) {
            ApngLogger.w(ApngLogTags.CACHE, "DiskLruCache: Failed to create cache directory: ${e.message}")
        }
    }

    /**
     * Get cache file for the given key.
     */
    suspend fun get(key: String): ByteArray? = mutex.withLock {
        val path = cacheFile(key)
        try {
            val bytes = fileSystem.read(path) { readByteArray() }
            accessTimes[key] = ++accessCounter
            ApngLogger.v(ApngLogTags.CACHE) { "DiskLruCache: Get hit for key=$key, size=${bytes.size}" }
            bytes
        } catch (e: FileNotFoundException) {
            ApngLogger.v(ApngLogTags.CACHE) { "DiskLruCache: Get miss for key=$key (file not found)" }
            null
        } catch (e: Exception) {
            ApngLogger.w(ApngLogTags.CACHE, "DiskLruCache: Get error for key=$key: ${e.message}")
            null
        }
    }

    /**
     * Put data in cache.
     */
    suspend fun put(key: String, bytes: ByteArray): Path = mutex.withLock {
        val path = cacheFile(key)
        val size = bytes.size.toLong()

        // Remove old entry if exists
        try {
            val oldSize = fileSystem.metadata(path).size ?: 0L
            fileSystem.delete(path)
            currentSize -= oldSize
            ApngLogger.v(ApngLogTags.CACHE) { "DiskLruCache: Removed old entry for key=$key, size=$oldSize" }
        } catch (_: FileNotFoundException) {
            // File doesn't exist, no need to remove
        } catch (_: Exception) {
            // Ignore other errors
        }

        // Add new entry
        fileSystem.write(path) { write(bytes) }
        currentSize += size
        accessTimes[key] = ++accessCounter
        ApngLogger.v(ApngLogTags.CACHE) { "DiskLruCache: Put key=$key, size=$size, currentSize=$currentSize" }

        // Evict oldest entries if cache exceeds limit
        evictIfNecessary()

        path
    }

    /**
     * Check if key exists in cache.
     */
    suspend fun contains(key: String): Boolean = mutex.withLock {
        try {
            fileSystem.metadata(cacheFile(key)).size != null
        } catch (_: FileNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Delete all cache entries.
     */
    suspend fun clear() = mutex.withLock {
        ApngLogger.d(ApngLogTags.CACHE, "DiskLruCache: Clearing all cache entries")
        try {
            fileSystem.listRecursively(directory).forEach { path ->
                try {
                    if (fileSystem.metadata(path).isRegularFile) {
                        fileSystem.delete(path)
                    }
                } catch (_: Exception) {
                    // Ignore individual file deletion errors
                }
            }
            accessTimes.clear()
            currentSize = 0L
            ApngLogger.i(ApngLogTags.CACHE, "DiskLruCache: Cache cleared successfully")
        } catch (e: Exception) {
            ApngLogger.w(ApngLogTags.CACHE, "DiskLruCache: Clear error: ${e.message}")
        }
    }

    /**
     * Get cache file path.
     */
    private fun cacheFile(key: String): Path {
        return directory / key.toSha256()
    }

    /**
     * Evict oldest entries if cache exceeds limit.
     */
    private fun evictIfNecessary() {
        if (currentSize <= maxSize) return

        ApngLogger.d(ApngLogTags.CACHE) { "DiskLruCache: Evicting entries, currentSize=$currentSize, maxSize=$maxSize" }
        
        // Sort by access time and remove oldest entries
        val sortedKeys = accessTimes.entries
            .sortedBy { it.value }
            .map { it.key }

        var evictedCount = 0
        for (key in sortedKeys) {
            if (currentSize <= maxSize * 0.9) break // Leave 10% buffer

            val path = cacheFile(key)
            try {
                val size = fileSystem.metadata(path).size ?: 0L
                fileSystem.delete(path)
                currentSize -= size
                accessTimes.remove(key)
                evictedCount++
            } catch (_: FileNotFoundException) {
                // Already deleted
                accessTimes.remove(key)
            } catch (_: Exception) {
                // Ignore errors
            }
        }
        
        if (evictedCount > 0) {
            ApngLogger.d(ApngLogTags.CACHE) { "DiskLruCache: Evicted $evictedCount entries, currentSize=$currentSize" }
        }
    }
}

/**
 * Generate SHA256 hash for a string.
 */
private fun String.toSha256(): String {
    return encodeUtf8().sha256().hex()
}

/**
 * Get the SHA256 hash of the UTF-8 encoded string.
 */
internal fun String.sha256(): String {
    return encodeUtf8().sha256().hex()
}

/**
 * Get the platform-specific file system.
 * Returns FileSystem.SYSTEM on JVM/Native, throws on Web.
 */
internal expect fun getSystemFileSystem(): FileSystem
