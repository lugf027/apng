package io.github.lugf027.apng.network

import okio.Path

/**
 * Strategy for caching APNG files.
 * Implementations can provide different caching strategies (memory, disk, hybrid, etc.)
 */
interface ApngCacheStrategy {
    /**
     * Get the cache path for the given URL.
     * Returns null if not in cache or caching is disabled.
     */
    fun path(url: String): Path?

    /**
     * Save APNG bytes to cache for the given URL.
     *
     * @param url The source URL
     * @param bytes The APNG data to cache
     * @return The path where the data was cached, or null if not cached
     */
    suspend fun save(url: String, bytes: ByteArray): Path?

    /**
     * Load APNG bytes from cache for the given URL.
     *
     * @param url The source URL
     * @return The cached bytes, or null if not in cache
     */
    suspend fun load(url: String): ByteArray?

    /**
     * Clear all cached entries.
     */
    suspend fun clear()
}
