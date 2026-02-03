package io.github.lugf027.apng.resources

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.lugf027.apng.compose.ApngCompositionResult
import io.github.lugf027.apng.compose.ApngCompositionSpec
import io.github.lugf027.apng.compose.rememberApngComposition
import io.github.lugf027.apng.compose.rememberApngPainter

/**
 * Remember and load APNG composition from Compose Resources.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val result = rememberApngCompositionFromResource(
 *         resourcePath = "animation.apng",
 *         readBytes = Res::readBytes
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
 * @param resourcePath The resource path relative to files directory
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @param directory The directory in composeResources (default: "files")
 * @return The loading result
 */
@Composable
fun rememberApngCompositionFromResource(
    resourcePath: String,
    readBytes: suspend (path: String) -> ByteArray,
    directory: String = "files"
): ApngCompositionResult {
    return rememberApngComposition {
        ApngCompositionSpec.Resource(resourcePath, readBytes, directory)
    }
}

/**
 * Simplified Composable to display APNG from Compose Resources with built-in loading and error states.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     ApngImageFromResource(
 *         resourcePath = "animation.apng",
 *         readBytes = Res::readBytes,
 *         contentDescription = "Loading animation"
 *     )
 * }
 * ```
 * 
 * @param resourcePath The resource path relative to files directory
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @param directory The directory in composeResources (default: "files")
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier
 * @param contentScale Content scale mode
 * @param autoPlay Whether to auto-play the animation
 * @param speed Playback speed multiplier
 * @param iterations Number of loops, 0 means infinite
 * @param onLoading Composable to show during loading
 * @param onError Composable to show on error
 */
@Composable
fun ApngImageFromResource(
    resourcePath: String,
    readBytes: suspend (path: String) -> ByteArray,
    directory: String = "files",
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0,
    onLoading: @Composable () -> Unit = {
        CircularProgressIndicator()
    },
    onError: @Composable (Throwable) -> Unit = { throwable ->
        Text(
            "Failed to load: ${throwable.message}",
            color = MaterialTheme.colorScheme.error
        )
    }
) {
    val compositionResult = rememberApngComposition {
        ApngCompositionSpec.Resource(resourcePath, readBytes, directory)
    }
    
    when (compositionResult) {
        is ApngCompositionResult.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                onLoading()
            }
        }
        is ApngCompositionResult.Error -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                onError(compositionResult.throwable)
            }
        }
        is ApngCompositionResult.Success -> {
            val painter = rememberApngPainter(
                composition = compositionResult.composition,
                autoPlay = autoPlay,
                speed = speed,
                iterations = iterations
            )
            
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}
