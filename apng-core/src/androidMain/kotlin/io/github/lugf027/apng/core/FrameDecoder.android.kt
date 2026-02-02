package io.github.lugf027.apng.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Android 实现的帧解码器
 */
class AndroidFrameDecoder : FrameDecoder {
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    override suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(frame.data, 0, frame.data.size)
                ?: Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            bitmapCache[frame.index] = bitmap
            bitmap
        } catch (e: Exception) {
            throw DecodingException("Failed to decode frame ${frame.index}", e)
        }
    }

    override fun release() {
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }
}

actual interface FrameDecoder {
    /**
     * 解码帧数据为 Android Bitmap
     */
    actual suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any

    /**
     * 释放资源
     */
    actual fun release()
}

actual fun createFrameDecoder(): FrameDecoder {
    return AndroidFrameDecoder()
}
