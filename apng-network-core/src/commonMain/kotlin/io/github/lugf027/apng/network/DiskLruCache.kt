package io.github.lugf027.apng.network

import okio.ByteString.Companion.encodeUtf8
import okio.FileNotFoundException
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.math.min

/**
 * A simple LRU disk cache implementation using Okio.
 * Based on the design of compottie's DiskLruCache.
 *
 * @param directory The directory to store cache files
 * @param maxSize The maximum cache size in bytes
 */
internal class DiskLruCache(
    val directory: Path,
    val maxSize: Long = 100L * 1024L * 1024L, // 100 MB default
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    private val accessTimes = mutableMapOf<String, Long>()
    private var currentSize = 0L

    init {
        fileSystem.createDirectories(directory, mustCreate = false)
    }

    /**
     * Get cache file path for the given key.
     */
    fun get(key: String): ByteArray? = synchronized(this) {
        val path = cacheFile(key)
        return try {
            val bytes = fileSystem.read(path) { readByteArray() }
            accessTimes[key] = System.currentTimeMillis()
            bytes
        } catch (e: FileNotFoundException) {
            null
        }
    }

    /**
     * Put data in cache.
     */
    fun put(key: String, bytes: ByteArray): Path = synchronized(this) {
        val path = cacheFile(key)
        val size = bytes.size.toLong()

        // Remove old entry if exists
        try {
            val oldSize = fileSystem.metadata(path).size ?: 0L
            fileSystem.delete(path)
            currentSize -= oldSize
        } catch (_: FileNotFoundException) {
            // File doesn't exist, no need to remove
        }

        // Add new entry
        fileSystem.write(path) { write(bytes) }
        currentSize += size
        accessTimes[key] = System.currentTimeMillis()

        // Evict oldest entries if cache exceeds limit
        evictIfNecessary()

        return path
    }

    /**
     * Check if key exists in cache.
     */
    fun contains(key: String): Boolean = synchronized(this) {
        return try {
            fileSystem.metadata(cacheFile(key)).size != null
        } catch (_: FileNotFoundException) {
            false
        }
    }

    /**
     * Delete all cache entries.
     */
    fun clear() = synchronized(this) {
        try {
            fileSystem.listRecursively(directory).forEach { path ->
                if (fileSystem.metadata(path).isRegularFile) {
                    fileSystem.delete(path)
                }
            }
            accessTimes.clear()
            currentSize = 0L
        } catch (e: Exception) {
            // Directory may not exist
        }
    }

    /**
     * Get cache file path.
     */
    private fun cacheFile(key: String): Path {
        return directory / key.sha256()
    }

    /**
     * Generate SHA256 hash for key.
     */
    private fun String.sha256(): String {
        return encodeUtf8().sha256().hex()
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
            } catch (e: FileNotFoundException) {
                // Already deleted
            }
        }
    }
}

/**
 * Get the SHA256 hash of the UTF-8 encoded string.
 */
internal fun String.sha256(): String {
    return encodeUtf8().sha256().hex()
}
