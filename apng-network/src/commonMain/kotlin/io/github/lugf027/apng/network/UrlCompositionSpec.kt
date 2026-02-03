package io.github.lugf027.apng.network

import io.github.lugf027.apng.compose.ApngComposition
import io.github.lugf027.apng.compose.ApngCompositionSpec

/**
 * Create an [ApngCompositionSpec] that loads from a network URL.
 * 
 * This is the convenient API with default Ktor HTTP client.
 * 
 * Example:
 * ```kotlin
 * val composition by rememberApngComposition {
 *     ApngCompositionSpec.Url("https://example.com/animation.apng")
 * }
 * ```
 * 
 * @param url The network URL to load from
 * @param cacheStrategy The cache strategy to use (default: disk cache)
 * @return An [ApngCompositionSpec] that loads from the URL
 */
public fun ApngCompositionSpec.Companion.Url(
    url: String,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy,
): ApngCompositionSpec {
    // Make sure network is initialized
    if (DefaultHttpClient !is KtorHttpClient) {
        initializeApngNetwork()
    }
    return Url(
        url = url,
        request = { requestUrl -> DefaultHttpClient.download(requestUrl) },
        cacheStrategy = cacheStrategy
    )
}

/**
 * Create an [ApngCompositionSpec] that loads from a network URL with custom request.
 * 
 * This API allows providing a custom HTTP request function.
 * 
 * Example:
 * ```kotlin
 * val composition by rememberApngComposition {
 *     ApngCompositionSpec.Url(
 *         url = "https://example.com/animation.apng",
 *         request = { url -> myCustomHttpClient.get(url) }
 *     )
 * }
 * ```
 * 
 * @param url The network URL to load from
 * @param request The network request function
 * @param cacheStrategy The cache strategy to use (default: disk cache)
 * @return An [ApngCompositionSpec] that loads from the URL
 */
public fun ApngCompositionSpec.Companion.Url(
    url: String,
    request: suspend (url: String) -> ByteArray,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy,
): ApngCompositionSpec = NetworkCompositionSpec(
    url = url,
    request = request,
    cacheStrategy = cacheStrategy
)

/**
 * Internal implementation of URL-based ApngCompositionSpec.
 */
private class NetworkCompositionSpec(
    private val url: String,
    private val request: suspend (url: String) -> ByteArray,
    private val cacheStrategy: ApngCacheStrategy
) : ApngCompositionSpec {

    override val key: String
        get() = "url_$url"

    override suspend fun load(): ApngComposition {
        val bytes = networkLoad(request, cacheStrategy, url)
        
        return checkNotNull(bytes?.let { ApngComposition.parse(it) }) {
            "Failed to load APNG from $url"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkCompositionSpec

        if (url != other.url) return false
        if (request != other.request) return false
        if (cacheStrategy != other.cacheStrategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "Url(url='$url', key=$key)"
    }
}
