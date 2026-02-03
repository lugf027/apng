package io.github.lugf027.apng.network

import androidx.compose.runtime.Composable
import io.github.lugf027.apng.compose.ApngCompositionResult
import io.github.lugf027.apng.compose.ApngCompositionSpec
import io.github.lugf027.apng.compose.rememberApngComposition

/**
 * Remember and load an APNG composition from a URL.
 *
 * This is a convenience wrapper around [rememberApngComposition] with [ApngCompositionSpec.Url].
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val result = rememberApngCompositionFromUrl(
 *         url = "https://example.com/animation.apng"
 *     )
 *     
 *     when (result) {
 *         is ApngCompositionResult.Loading -> CircularProgressIndicator()
 *         is ApngCompositionResult.Success -> {
 *             val painter = rememberApngPainter(result.composition)
 *             Image(painter = painter, contentDescription = null)
 *         }
 *         is ApngCompositionResult.Error -> Text("Failed to load: ${result.throwable.message}")
 *     }
 * }
 * ```
 * 
 * @param url The URL to load from
 * @param cacheStrategy The cache strategy to use
 * @return The loading result
 */
@Composable
fun rememberApngCompositionFromUrl(
    url: String,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy
): ApngCompositionResult {
    return rememberApngComposition {
        ApngCompositionSpec.Url(url, cacheStrategy)
    }
}

/**
 * Remember and load an APNG composition from a URL with custom request.
 *
 * @param url The URL to load from
 * @param request Custom network request function
 * @param cacheStrategy The cache strategy to use
 * @return The loading result
 */
@Composable
fun rememberApngCompositionFromUrl(
    url: String,
    request: suspend (url: String) -> ByteArray,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy
): ApngCompositionResult {
    return rememberApngComposition {
        ApngCompositionSpec.Url(url, request, cacheStrategy)
    }
}
