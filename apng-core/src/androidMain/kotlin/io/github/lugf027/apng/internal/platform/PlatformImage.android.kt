package io.github.lugf027.apng.internal.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalArgumentException("Failed to decode PNG bytes on Android")
    return bitmap.asImageBitmap()
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
    val currentBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(currentBitmap)
    var previousBitmap: Bitmap? = null
    val srcPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC) }
    val overPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) }
    val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    for (i in 0 until frameCount) {
        val disposeOp = getFrameDisposeOp(i)
        val blendOp = getFrameBlendOp(i)
        val xOffset = getFrameXOffset(i)
        val yOffset = getFrameYOffset(i)
        val frameWidth = getFrameWidth(i)
        val frameHeight = getFrameHeight(i)

        // Save state before drawing if PREVIOUS dispose
        if (disposeOp == 2) { // PREVIOUS
            previousBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        // Decode frame
        val pngBytes = getFramePngBytes(i)
        val frameBitmap = BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.size)
            ?: continue

        // Draw frame onto canvas
        val dstRect = Rect(xOffset, yOffset, xOffset + frameWidth, yOffset + frameHeight)
        val srcRect = Rect(0, 0, frameBitmap.width, frameBitmap.height)

        when (blendOp) {
            0 -> { // SOURCE - clear the region first, then draw
                canvas.drawRect(
                    dstRect.left.toFloat(), dstRect.top.toFloat(),
                    dstRect.right.toFloat(), dstRect.bottom.toFloat(),
                    clearPaint
                )
                canvas.drawBitmap(frameBitmap, srcRect, dstRect, srcPaint)
            }
            1 -> { // OVER - alpha composite
                canvas.drawBitmap(frameBitmap, srcRect, dstRect, overPaint)
            }
        }
        frameBitmap.recycle()

        // Snapshot current state as composed frame
        result.add(currentBitmap.copy(Bitmap.Config.ARGB_8888, false).asImageBitmap())

        // Apply dispose operation for next frame
        when (disposeOp) {
            0 -> { /* NONE - keep current state */ }
            1 -> { // BACKGROUND - clear the frame region
                canvas.drawRect(
                    xOffset.toFloat(), yOffset.toFloat(),
                    (xOffset + frameWidth).toFloat(), (yOffset + frameHeight).toFloat(),
                    clearPaint
                )
            }
            2 -> { // PREVIOUS - restore previous state
                previousBitmap?.let {
                    canvas.drawBitmap(it, 0f, 0f, srcPaint)
                    it.recycle()
                    previousBitmap = null
                }
            }
        }
    }

    currentBitmap.recycle()
    return result
}
