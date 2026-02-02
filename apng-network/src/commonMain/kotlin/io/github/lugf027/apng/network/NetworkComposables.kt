package io.github.lugf027.apng.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.lugf027.apng.core.ApngImage
import io.github.lugf027.apng.core.ApngLoader
import kotlinx.coroutines.launch

/**
 * Remember and load an APNG composition from a URL.
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
 *         is ApngCompositionResult.Success -> ApngImage(result.composition)
 *         is ApngCompositionResult.Error -> Text("Failed to load: ${result.exception.message}")
 *     }
 * }
 * ```
 */
@Composable
fun rememberApngCompositionFromUrl(
    url: String,
    httpClient: HttpClient = DefaultHttpClient,
    cacheStrategy: ApngCacheStrategy = DefaultCacheStrategy
): ApngCompositionResult {
    val result = remember { mutableStateOf<ApngCompositionResult>(ApngCompositionResult.Loading()) }

    LaunchedEffect(url) {
        result.value = ApngCompositionResult.Loading()
        
        launch {
            try {
                // Make sure network is initialized
                if (DefaultHttpClient !is KtorHttpClient) {
                    initializeApngNetwork()
                }

                val apngImage = ApngLoader().loadFromUrl(
                    url = url,
                    httpClient = httpClient,
                    cacheStrategy = cacheStrategy,
                    onProgress = { downloaded, total ->
                        if (total > 0) {
                            val progress = downloaded.toFloat() / total
                            result.value = ApngCompositionResult.Loading(progress)
                        }
                    }
                )
                result.value = ApngCompositionResult.Success(apngImage)
            } catch (e: Exception) {
                result.value = ApngCompositionResult.Error(e)
            }
        }
    }

    return result.value
}

/**
 * Simplified API to check if the composition is ready.
 */
fun ApngCompositionResult.isLoading(): Boolean = this is ApngCompositionResult.Loading

/**
 * Get the loaded composition or null if not loaded yet.
 */
fun ApngCompositionResult.getOrNull(): ApngImage? =
    (this as? ApngCompositionResult.Success)?.composition

/**
 * Get the error or null if no error.
 */
fun ApngCompositionResult.getErrorOrNull(): Exception? =
    (this as? ApngCompositionResult.Error)?.exception
