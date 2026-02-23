package io.github.lugf027.apng

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize

@Composable
public fun rememberApngPainter(
    composition: ApngComposition?,
    progress: () -> Float,
    clipToCompositionBounds: Boolean = true,
): Painter {
    return remember(composition) {
        if (composition != null) {
            ApngPainterImpl(composition, progress, clipToCompositionBounds)
        } else {
            EmptyPainter
        }
    }
}

@Composable
public fun rememberApngPainter(
    composition: ApngComposition?,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    clipSpec: ApngClipSpec? = null,
    speed: Float = composition?.speed ?: 1f,
    iterations: Int = composition?.iterations ?: 1,
    cancellationBehavior: ApngCancellationBehavior = ApngCancellationBehavior.Immediately,
    clipToCompositionBounds: Boolean = true,
): Painter {
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

    return rememberApngPainter(
        composition = composition,
        progress = { state.progress },
        clipToCompositionBounds = clipToCompositionBounds,
    )
}

@Stable
private class ApngPainterImpl(
    private val composition: ApngComposition,
    private val progress: () -> Float,
    private val clipToCompositionBounds: Boolean,
) : Painter() {

    override val intrinsicSize: Size
        get() = Size(composition.width.toFloat(), composition.height.toFloat())

    override fun DrawScope.onDraw() {
        if (composition.frames.isEmpty()) return

        val frameIndex = composition.frameIndexAt(progress())
        val frame = composition.frames[frameIndex]

        if (clipToCompositionBounds) {
            clipRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
            ) {
                drawImage(
                    image = frame,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                )
            }
        } else {
            drawImage(
                image = frame,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            )
        }
    }
}

private object EmptyPainter : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified
    override fun DrawScope.onDraw() {}
}
