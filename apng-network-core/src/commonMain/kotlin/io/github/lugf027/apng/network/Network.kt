package io.github.lugf027.apng.network

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
    return try {
        // 1. Try to load from cache
        try {
            cacheStrategy.load(url)?.let {
                return it
            }
        } catch (_: Throwable) {
            // Ignore cache errors
        }

        // 2. Download from network
        val bytes = request(url)

        // 3. Save to cache
        try {
            cacheStrategy.save(url, bytes)
        } catch (e: Throwable) {
            println("Failed to cache downloaded APNG: ${e.message}")
        }
        
        bytes
    } catch (t: Throwable) {
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
        throw UnsupportedOperationException(
            "Default HTTP client not initialized. Make sure to use apng-network module " +
            "or provide a custom HttpClient via DefaultHttpClient."
        )
    }
}
