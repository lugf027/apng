package io.github.lugf027.apng.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * APNG image display component.
 *
 * Uses Compose Painter for rendering, inspired by compottie's design.
 *
 * @param data APNG image data as bytes
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier
 * @param contentScale Content scale mode
 * @param autoPlay Whether to auto-play the animation
 * @param speed Playback speed multiplier
 * @param iterations Number of loops, 0 means infinite
 * @param onError Error callback
 */
@Composable
fun ApngImage(
    data: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0,
    onError: ((Throwable) -> Unit)? = null
) {
    val compositionResult = rememberApngComposition(data)

    when {
        compositionResult.isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        compositionResult.isFailure -> {
            compositionResult.error?.let { onError?.invoke(it) }
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${compositionResult.error?.message}")
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

/**
 * APNG image display component - using a spec.
 *
 * This is the recommended API for loading APNG from various sources.
 *
 * Example:
 * ```kotlin
 * ApngImage(
 *     spec = { ApngCompositionSpec.Url("https://example.com/anim.apng") },
 *     contentDescription = "Animation"
 * )
 * ```
 *
 * @param spec Lambda that returns the composition spec
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier
 * @param contentScale Content scale mode
 * @param autoPlay Whether to auto-play the animation
 * @param speed Playback speed multiplier
 * @param iterations Number of loops, 0 means infinite
 * @param onError Error callback
 */
@Composable
fun ApngImage(
    spec: suspend () -> ApngCompositionSpec,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0,
    onError: ((Throwable) -> Unit)? = null
) {
    val compositionResult = rememberApngComposition(spec = spec)

    when {
        compositionResult.isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        compositionResult.isFailure -> {
            compositionResult.error?.let { onError?.invoke(it) }
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${compositionResult.error?.message}")
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

/**
 * APNG image display component - using pre-loaded composition data.
 *
 * @param composition APNG composition data
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier
 * @param contentScale Content scale mode
 * @param autoPlay Whether to auto-play the animation
 * @param speed Playback speed multiplier
 * @param iterations Number of loops, 0 means infinite
 */
@Composable
fun ApngImage(
    composition: ApngComposition,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0
) {
    val painter = rememberApngPainter(
        composition = composition,
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

/**
 * APNG image display component - using custom progress control.
 *
 * @param composition APNG composition data
 * @param progress Current playback progress lambda (0.0-1.0)
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier
 * @param contentScale Content scale mode
 */
@Composable
fun ApngImage(
    composition: ApngComposition?,
    progress: () -> Float,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val painter = rememberApngPainter(
        composition = composition,
        progress = progress
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
