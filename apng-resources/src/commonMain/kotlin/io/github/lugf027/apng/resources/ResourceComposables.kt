package io.github.lugf027.apng.resources

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.lugf027.apng.compose.ApngCompositionResult
import io.github.lugf027.apng.compose.ApngCompositionSpec
import io.github.lugf027.apng.compose.rememberApngComposition
import io.github.lugf027.apng.compose.rememberApngPainter

/**
 * Remember and load APNG composition from Compose Resources.
 *
 * [`Res.readBytes`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html#raw-files)
 * should be used as a [readBytes] source.
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
 *     when {
 *         result.isLoading -> CircularProgressIndicator()
 *         result.isSuccess -> {
 *             val painter = rememberApngPainter(result.value)
 *             Image(painter = painter, contentDescription = null)
 *         }
 *         result.isFailure -> Text("Failed to load: ${result.error?.message}")
 *     }
 * }
 * ```
 *
 * @param resourcePath The resource path relative to [directory], e.g. "animation.apng"
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @param directory The directory in composeResources (default: "files")
 * @return The loading result containing composition or error
 */
@Composable
public fun rememberApngCompositionFromResource(
    resourcePath: String,
    readBytes: suspend (path: String) -> ByteArray,
    directory: String = "files"
): ApngCompositionResult {
    return rememberApngComposition {
        ApngCompositionSpec.Resource(resourcePath, readBytes, directory)
    }
}

/**
 * Remember and load APNG composition from Compose Resources using full path.
 *
 * This is an alternative API that provides more flexibility in path handling.
 * [`Res.readBytes`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html#raw-files)
 * should be used as a [readBytes] source.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     // Using full path
 *     val result = rememberApngCompositionFromResourceBytes(
 *         path = "files/animation.apng",
 *         directory = "",  // empty since path already includes directory
 *         readBytes = Res::readBytes
 *     )
 *
 *     // Or with directory prefix
 *     val result2 = rememberApngCompositionFromResourceBytes(
 *         path = "animation.apng",
 *         directory = "files",  // will be prepended to path
 *         readBytes = Res::readBytes
 *     )
 *
 *     when {
 *         result.isLoading -> CircularProgressIndicator()
 *         result.isSuccess -> {
 *             val painter = rememberApngPainter(result.value)
 *             Image(painter = painter, contentDescription = null)
 *         }
 *         result.isFailure -> Text("Failed to load: ${result.error?.message}")
 *     }
 * }
 * ```
 *
 * @param path The resource path, can be full path like "files/animation.apng" or relative path
 * @param directory The directory prefix to prepend to [path] (default: "files"). Set to empty string if [path] is already a full path.
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @return The loading result containing composition or error
 *
 * @see rememberApngCompositionFromResource for the original API
 */
@Composable
public fun rememberApngCompositionFromResourceBytes(
    path: String,
    directory: String = "files",
    readBytes: suspend (path: String) -> ByteArray,
): ApngCompositionResult {
    return rememberApngComposition {
        ApngCompositionSpec.ResourceBytes(path, directory, readBytes)
    }
}

/**
 * Simplified Composable to display APNG from Compose Resources with built-in loading and error states.
 *
 * [`Res.readBytes`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html#raw-files)
 * should be used as a [readBytes] source.
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
 * @param resourcePath The resource path relative to [directory], e.g. "animation.apng"
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @param directory The directory in composeResources (default: "files")
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier for the image
 * @param contentScale Content scale mode for the image
 * @param autoPlay Whether to auto-play the animation (default: true)
 * @param speed Playback speed multiplier (default: 1f)
 * @param iterations Number of loops, 0 means infinite (default: 0)
 * @param onLoading Composable to show during loading
 * @param onError Composable to show on error
 */
@Composable
public fun ApngImageFromResource(
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
    ApngImageFromResourceInternal(
        spec = { ApngCompositionSpec.Resource(resourcePath, readBytes, directory) },
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        autoPlay = autoPlay,
        speed = speed,
        iterations = iterations,
        onLoading = onLoading,
        onError = onError
    )
}

/**
 * Simplified Composable to display APNG from Compose Resources using full path,
 * with built-in loading and error states.
 *
 * This is an alternative API that provides more flexibility in path handling.
 * [`Res.readBytes`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html#raw-files)
 * should be used as a [readBytes] source.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     // Using full path
 *     ApngImageFromResourceBytes(
 *         path = "files/animation.apng",
 *         directory = "",  // empty since path already includes directory
 *         readBytes = Res::readBytes,
 *         contentDescription = "Loading animation"
 *     )
 *
 *     // Or with directory prefix
 *     ApngImageFromResourceBytes(
 *         path = "animation.apng",
 *         directory = "files",  // will be prepended to path
 *         readBytes = Res::readBytes,
 *         contentDescription = "Loading animation"
 *     )
 * }
 * ```
 *
 * @param path The resource path, can be full path like "files/animation.apng" or relative path
 * @param directory The directory prefix to prepend to [path] (default: "files"). Set to empty string if [path] is already a full path.
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier for the image
 * @param contentScale Content scale mode for the image
 * @param autoPlay Whether to auto-play the animation (default: true)
 * @param speed Playback speed multiplier (default: 1f)
 * @param iterations Number of loops, 0 means infinite (default: 0)
 * @param onLoading Composable to show during loading
 * @param onError Composable to show on error
 *
 * @see ApngImageFromResource for the original API
 */
@Composable
public fun ApngImageFromResourceBytes(
    path: String,
    directory: String = "files",
    readBytes: suspend (path: String) -> ByteArray,
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
    ApngImageFromResourceInternal(
        spec = { ApngCompositionSpec.ResourceBytes(path, directory, readBytes) },
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        autoPlay = autoPlay,
        speed = speed,
        iterations = iterations,
        onLoading = onLoading,
        onError = onError
    )
}

/**
 * Internal implementation for APNG image display with loading/error states.
 */
@Composable
private fun ApngImageFromResourceInternal(
    spec: suspend () -> ApngCompositionSpec,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    autoPlay: Boolean,
    speed: Float,
    iterations: Int,
    onLoading: @Composable () -> Unit,
    onError: @Composable (Throwable) -> Unit
) {
    val compositionResult = rememberApngComposition(spec = spec)

    when {
        compositionResult.isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                onLoading()
            }
        }
        compositionResult.isFailure -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                compositionResult.error?.let { onError(it) }
            }
        }
        compositionResult.isSuccess -> {
            val painter = rememberApngPainter(
                composition = compositionResult.value,
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
