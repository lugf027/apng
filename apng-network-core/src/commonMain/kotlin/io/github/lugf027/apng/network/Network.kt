package io.github.lugf027.apng.network

import io.github.lugf027.apng.core.ApngImage
import io.github.lugf027.apng.core.ApngLoader

/**
 * Default HTTP client instance. Can be overridden if needed.
 */
var DefaultHttpClient: HttpClient = DefaultHttpClientImpl()

/**
 * Default cache strategy instance.
 */
var DefaultCacheStrategy: ApngCacheStrategy = DiskApngCacheStrategy.Instance

/**
 * Default resource loader instance.
 */
private var _resourceLoader: ApngResourceLoader? = null

/**
 * Get or create the default resource loader.
 */
suspend fun getDefaultResourceLoader(): ApngResourceLoader {
    return _resourceLoader ?: createResourceLoader().also {
        _resourceLoader = it
    }
}

/**
 * Load APNG from a network URL with automatic caching.
 *
 * @param loader The ApngLoader instance
 * @param url The URL to load from
 * @param httpClient The HTTP client to use (defaults to DefaultHttpClient)
 * @param cacheStrategy The cache strategy to use (defaults to DefaultCacheStrategy)
 * @param onProgress Progress callback with (downloaded, total) bytes
 * @return The loaded ApngImage
 * @throws Exception if loading fails after retries
 */
suspend fun loadFromUrl(
    loader: ApngLoader,
    url: String,
    httpClient: HttpClient = DefaultHttpClient,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy,
    onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
): ApngImage {
    // 1. Try to load from cache
    cacheStrategy.load(url)?.let { bytes ->
        return loader.loadFromBytes(bytes)
    }

    // 2. Download from network
    val bytes = httpClient.download(url, onProgress)

    // 3. Save to cache (non-blocking)
    try {
        cacheStrategy.save(url, bytes)
    } catch (e: Exception) {
        // Log but don't fail - continue with loaded bytes
        println("Failed to cache APNG: ${e.message}")
    }

    // 4. Parse and return
    return loader.loadFromBytes(bytes)
}

/**
 * Load APNG from any source using the resource loader.
 *
 * @param loader The ApngLoader instance
 * @param source The data source
 * @param httpClient The HTTP client for URL sources
 * @param cacheStrategy The cache strategy for URL sources
 * @param onProgress Progress callback for URL sources
 * @return The loaded ApngImage
 * @throws Exception if loading fails
 */
suspend fun loadFromSource(
    loader: ApngLoader,
    source: ApngSource,
    httpClient: HttpClient = DefaultHttpClient,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy,
    onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
): ApngImage {
    return when (source) {
        is ApngSource.Bytes -> loader.loadFromBytes(source.data)
        is ApngSource.Url -> loadFromUrl(loader, source.url, httpClient, cacheStrategy, onProgress)
        else -> {
            val resourceLoader = getDefaultResourceLoader()
            val bytes = resourceLoader.load(source)
            loader.loadFromBytes(bytes)
        }
    }
}

/**
 * Default HTTP client implementation - must be provided by the network module.
 * This is a placeholder that will be overridden.
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
