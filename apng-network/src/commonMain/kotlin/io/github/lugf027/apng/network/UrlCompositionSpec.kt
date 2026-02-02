package io.github.lugf027.apng.network

import io.github.lugf027.apng.core.ApngImage
import io.github.lugf027.apng.core.ApngLoader

/**
 * Utility functions for creating URL-based APNG loading specs.
 * Provides convenient shortcuts for common loading scenarios.
 */

/**
 * Load APNG from a network URL with default settings.
 *
 * Example:
 * ```kotlin
 * val image = ApngLoader().loadFromUrl(
 *     url = "https://example.com/animation.apng"
 * )
 * ```
 *
 * @param url The URL to load from
 * @param httpClient The HTTP client to use
 * @param cacheStrategy The cache strategy to use
 * @param onProgress Progress callback
 * @return The loaded APNG image
 */
suspend fun ApngLoader.loadFromUrl(
    url: String,
    httpClient: HttpClient = DefaultHttpClient,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy,
    onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
): ApngImage {
    // Make sure network is initialized
    if (DefaultHttpClient !is KtorHttpClient) {
        initializeApngNetwork()
    }
    return io.github.lugf027.apng.network.loadFromUrl(
        this,
        url,
        httpClient,
        cacheStrategy,
        onProgress
    )
}

/**
 * Load APNG from any source.
 *
 * Example:
 * ```kotlin
 * val source = ApngSource.Url("https://example.com/animation.apng")
 * val image = ApngLoader().loadFromSource(source)
 * ```
 */
suspend fun ApngLoader.loadFromSource(
    source: ApngSource,
    httpClient: HttpClient = DefaultHttpClient,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy,
    onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
): ApngImage {
    return io.github.lugf027.apng.network.loadFromSource(
        this,
        source,
        httpClient,
        cacheStrategy,
        onProgress
    )
}

/**
 * Create an ApngSource for a URL.
 *
 * Example:
 * ```kotlin
 * val source = ApngSource.Url("https://example.com/animation.apng")
 * val image = ApngLoader().loadFromSource(source)
 * ```
 */
fun urlSource(url: String): ApngSource.Url = ApngSource.Url(url)

/**
 * Create an ApngSource for bytes.
 */
fun bytesSource(data: ByteArray): ApngSource.Bytes = ApngSource.Bytes(data)

/**
 * Create an ApngSource for a file.
 */
fun fileSource(path: String): ApngSource.File = ApngSource.File(path)

/**
 * Create an ApngSource for a resource.
 */
fun resourceSource(resourcePath: String): ApngSource.Resource = ApngSource.Resource(resourcePath)
