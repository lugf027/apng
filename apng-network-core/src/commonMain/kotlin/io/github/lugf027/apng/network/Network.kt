package io.github.lugf027.apng.network

import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger

/**
 * Default HTTP client instance. Can be overridden if needed.
 */
var DefaultHttpClient: HttpClient = DefaultHttpClientImpl()

/**
 * Default cache strategy instance.
 */
var DefaultCacheStrategy: ApngCacheStrategy = DiskApngCacheStrategy.Instance

/**
 * Core network loading function.
 * Handles cache check, network request, and cache save.
 * 
 * @param request The network request function
 * @param cacheStrategy The cache strategy to use
 * @param url The URL to load from
 * @return Loaded bytes (null if failed)
 */
suspend fun networkLoad(
    request: suspend (url: String) -> ByteArray,
    cacheStrategy: ApngCacheStrategy,
    url: String
): ByteArray? {
    ApngLogger.d(ApngLogTags.NETWORK, "networkLoad: Starting load for URL: $url")
    return try {
        // 1. Try to load from cache
        try {
            cacheStrategy.load(url)?.let {
                ApngLogger.d(ApngLogTags.CACHE) { "networkLoad: Cache hit for URL: $url, size: ${it.size} bytes" }
                return it
            }
            ApngLogger.v(ApngLogTags.CACHE, "networkLoad: Cache miss for URL: $url")
        } catch (e: Throwable) {
            ApngLogger.w(ApngLogTags.CACHE, "networkLoad: Cache load error for URL: $url - ${e.message}")
        }

        // 2. Download from network
        ApngLogger.d(ApngLogTags.NETWORK, "networkLoad: Downloading from network: $url")
        val bytes = request(url)
        ApngLogger.i(ApngLogTags.NETWORK) { "networkLoad: Downloaded ${bytes.size} bytes from: $url" }

        // 3. Save to cache
        try {
            cacheStrategy.save(url, bytes)
            ApngLogger.v(ApngLogTags.CACHE) { "networkLoad: Saved ${bytes.size} bytes to cache for URL: $url" }
        } catch (e: Throwable) {
            ApngLogger.w(ApngLogTags.CACHE, "Failed to cache downloaded APNG: ${e.message}", e)
        }
        
        bytes
    } catch (t: Throwable) {
        ApngLogger.e(ApngLogTags.NETWORK, "networkLoad: Failed to load from URL: $url", t)
        null
    }
}

/**
 * Default HTTP client implementation - placeholder.
 * This will be overridden by the network module.
 */
private class DefaultHttpClientImpl : HttpClient {
    override suspend fun download(
        url: String,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): ByteArray {
        ApngLogger.e(ApngLogTags.NETWORK, "Default HTTP client not initialized")
        throw UnsupportedOperationException(
            "Default HTTP client not initialized. Make sure to use apng-network module " +
            "or provide a custom HttpClient via DefaultHttpClient."
        )
    }
}
