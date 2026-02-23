package io.github.lugf027.apng.internal.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.BlendMode as SkiaBlendMode
import org.jetbrains.skia.Paint as SkiaPaint
import org.jetbrains.skia.Rect as SkiaRect

internal actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

internal actual fun composeFrames(
    canvasWidth: Int,
    canvasHeight: Int,
    frameCount: Int,
    getFramePngBytes: (index: Int) -> ByteArray,
    getFrameXOffset: (index: Int) -> Int,
    getFrameYOffset: (index: Int) -> Int,
    getFrameWidth: (index: Int) -> Int,
    getFrameHeight: (index: Int) -> Int,
    getFrameDisposeOp: (index: Int) -> Int,
    getFrameBlendOp: (index: Int) -> Int,
): List<ImageBitmap> {
    val result = mutableListOf<ImageBitmap>()

    val currentBitmap = Bitmap().apply {
        allocPixels(ImageInfo.makeN32(canvasWidth, canvasHeight, ColorAlphaType.PREMUL))
        erase(0)
    }
    val canvas = Canvas(currentBitmap)
    var previousSnapshot: ByteArray? = null

    val srcPaint = SkiaPaint().apply { blendMode = SkiaBlendMode.SRC }
    val overPaint = SkiaPaint().apply { blendMode = SkiaBlendMode.SRC_OVER }

    for (i in 0 until frameCount) {
        val disposeOp = getFrameDisposeOp(i)
        val blendOp = getFrameBlendOp(i)
        val xOffset = getFrameXOffset(i).toFloat()
        val yOffset = getFrameYOffset(i).toFloat()
        val frameWidth = getFrameWidth(i).toFloat()
        val frameHeight = getFrameHeight(i).toFloat()
        val dstRect = SkiaRect.makeXYWH(xOffset, yOffset, frameWidth, frameHeight)

        // Save state before drawing if PREVIOUS dispose
        if (disposeOp == 2) {
            previousSnapshot = currentBitmap.readPixels()
        }

        // Decode frame
        val frameImage = Image.makeFromEncoded(getFramePngBytes(i)) ?: continue

        // Draw frame onto canvas
        when (blendOp) {
            0 -> { // SOURCE - replace the region
                canvas.save()
                canvas.clipRect(dstRect)
                canvas.clear(0)
                canvas.restore()
                canvas.drawImageRect(frameImage, dstRect, srcPaint)
            }
            1 -> { // OVER - alpha composite
                canvas.drawImageRect(frameImage, dstRect, overPaint)
            }
        }

        // Snapshot current canvas as composed frame
        val pixels = currentBitmap.readPixels()
        if (pixels != null) {
            val snapshot = Bitmap().apply {
                allocPixels(currentBitmap.imageInfo)
                installPixels(pixels)
            }
            result.add(Image.makeFromBitmap(snapshot).toComposeImageBitmap())
        }

        // Apply dispose operation for next frame
        when (disposeOp) {
            0 -> { /* NONE - keep */ }
            1 -> { // BACKGROUND - clear frame region
                canvas.save()
                canvas.clipRect(dstRect)
                canvas.clear(0)
                canvas.restore()
            }
            2 -> { // PREVIOUS - restore
                previousSnapshot?.let { prevPixels ->
                    currentBitmap.installPixels(prevPixels)
                    previousSnapshot = null
                }
            }
        }
    }

    return result
}
