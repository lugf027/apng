package io.github.lugf027.apng

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale

/**
 * A composable that displays an APNG animation with automatic playback control.
 *
 * This is a convenience wrapper around [rememberApngPainter] and [Image] that provides
 * a unified API consistent with the standard Compose [Image] component, while also
 * exposing APNG-specific animation parameters.
 *
 * @param composition The [ApngComposition] to render. Pass null while loading;
 *   an empty painter will be used until the composition is available.
 * @param contentDescription Text used by accessibility services to describe what this image represents.
 * @param modifier Modifier to apply to this layout node.
 * @param alignment Alignment within the bounds when the content is smaller than the layout size.
 * @param contentScale Scale strategy for the content within the bounds.
 * @param alpha Opacity to apply to the image.
 * @param colorFilter Optional [ColorFilter] to apply to the image.
 * @param isPlaying Whether the animation is currently playing.
 * @param restartOnPlay Whether to restart the animation from the beginning when [isPlaying] changes to true.
 * @param reverseOnRepeat Whether to reverse the animation direction on each repeat.
 * @param clipSpec Optional [ApngClipSpec] to clip the animation to a sub-range.
 * @param speed Playback speed multiplier. Defaults to the value embedded in the composition, or 1f.
 * @param iterations Number of times to play the animation.
 *   Defaults to the value embedded in the composition, or 1.
 *   Use [Apng.IterateForever] for infinite looping.
 * @param cancellationBehavior Behavior when the animation is cancelled.
 * @param clipToCompositionBounds Whether to clip drawing to the composition bounds.
 *
 * @see rememberApngPainter
 * @see ApngImage overload accepting a [progress] lambda for manual control
 */
@Composable
public fun ApngImage(
    composition: ApngComposition?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    clipSpec: ApngClipSpec? = null,
    speed: Float = composition?.speed ?: 1f,
    iterations: Int = composition?.iterations ?: 1,
    cancellationBehavior: ApngCancellationBehavior = ApngCancellationBehavior.Immediately,
    clipToCompositionBounds: Boolean = true,
) {
    val state = animateApngCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = restartOnPlay,
        reverseOnRepeat = reverseOnRepeat,
        clipSpec = clipSpec,
        speed = speed,
        iterations = iterations,
        cancellationBehavior = cancellationBehavior,
    )
    ApngImage(
        composition = composition,
        progress = { state.progress },
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        clipToCompositionBounds = clipToCompositionBounds,
    )
}

/**
 * A composable that displays an APNG animation with manual progress control.
 *
 * Use this overload when you need full control over the animation progress, e.g., for
 * scrubbing, syncing with gestures, or driving the animation from an external source.
 * For automatic playback, use the [ApngImage] overload without a [progress] parameter.
 *
 * @param composition The [ApngComposition] to render. Pass null while loading;
 *   an empty painter will be used until the composition is available.
 * @param progress A lambda that returns the current animation progress in the range 0f..1f.
 * @param contentDescription Text used by accessibility services to describe what this image represents.
 * @param modifier Modifier to apply to this layout node.
 * @param alignment Alignment within the bounds when the content is smaller than the layout size.
 * @param contentScale Scale strategy for the content within the bounds.
 * @param alpha Opacity to apply to the image.
 * @param colorFilter Optional [ColorFilter] to apply to the image.
 * @param clipToCompositionBounds Whether to clip drawing to the composition bounds.
 *
 * @see rememberApngPainter
 */
@Composable
public fun ApngImage(
    composition: ApngComposition?,
    progress: () -> Float,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    clipToCompositionBounds: Boolean = true,
) {
    val painter = rememberApngPainter(
        composition = composition,
        progress = progress,
        clipToCompositionBounds = clipToCompositionBounds,
    )
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}
