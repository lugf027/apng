package io.github.lugf027.apng.core

import org.jetbrains.skia.Image

/**
 * Skiko 实现的帧解码器
 * 支持 Desktop (JVM) 和 iOS Native 渲染
 */
class SkikoFrameDecoder : FrameDecoder {
    private val imageCache = mutableMapOf<Int, Image>()

    override suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any {
        return try {
            val image = Image.makeFromEncoded(frame.data)
                ?: throw DecodingException("Failed to create image from frame data")
            imageCache[frame.index] = image
            image
        } catch (e: Exception) {
            throw DecodingException("Failed to decode frame ${frame.index}", e)
        }
    }

    override fun release() {
        imageCache.values.forEach { it.close() }
        imageCache.clear()
    }
}

actual interface FrameDecoder {
    actual suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any
    actual fun release()
}

actual fun createFrameDecoder(): FrameDecoder {
    return SkikoFrameDecoder()
}
