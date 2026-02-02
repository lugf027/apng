package io.github.lugf027.apng.network

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
        } catch (_: Exception) {
            // Directory creation may fail, but we'll handle errors during actual operations
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
            bytes
        } catch (e: FileNotFoundException) {
            null
        } catch (e: Exception) {
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
        } catch (_: FileNotFoundException) {
            // File doesn't exist, no need to remove
        } catch (_: Exception) {
            // Ignore other errors
        }

        // Add new entry
        fileSystem.write(path) { write(bytes) }
        currentSize += size
        accessTimes[key] = ++accessCounter

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
        } catch (_: Exception) {
            // Directory may not exist
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

        // Sort by access time and remove oldest entries
        val sortedKeys = accessTimes.entries
            .sortedBy { it.value }
            .map { it.key }

        for (key in sortedKeys) {
            if (currentSize <= maxSize * 0.9) break // Leave 10% buffer

            val path = cacheFile(key)
            try {
                val size = fileSystem.metadata(path).size ?: 0L
                fileSystem.delete(path)
                currentSize -= size
                accessTimes.remove(key)
            } catch (_: FileNotFoundException) {
                // Already deleted
                accessTimes.remove(key)
            } catch (_: Exception) {
                // Ignore errors
            }
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
