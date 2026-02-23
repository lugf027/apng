package io.github.lugf027.apng.internal.apng

import androidx.compose.ui.graphics.ImageBitmap
import io.github.lugf027.apng.internal.platform.composeFrames

internal object ApngFrameComposer {

    fun compose(data: ApngAnimationData): List<ComposedFrame> {
        val frames = data.frames
        if (frames.isEmpty()) return emptyList()

        val composedBitmaps = composeFrames(
            canvasWidth = data.canvasWidth,
            canvasHeight = data.canvasHeight,
            frameCount = frames.size,
            getFramePngBytes = { frames[it].pngBytes },
            getFrameXOffset = { frames[it].xOffset },
            getFrameYOffset = { frames[it].yOffset },
            getFrameWidth = { frames[it].width },
            getFrameHeight = { frames[it].height },
            getFrameDisposeOp = { frames[it].disposeOp.value.toInt() },
            getFrameBlendOp = { frames[it].blendOp.value.toInt() },
        )

        return composedBitmaps.mapIndexed { index, bitmap ->
            ComposedFrame(
                bitmap = bitmap,
                delayMs = frames[index].delayMs
            )
        }
    }
}

internal data class ComposedFrame(
    val bitmap: ImageBitmap,
    val delayMs: Float
)
