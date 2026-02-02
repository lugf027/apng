package io.github.lugf027.apng.core

import org.jetbrains.skia.Image

/**
 * Web 平台的帧解码器实现
 * 使用 Skiko 库（通过 Skia 绑定）进行 PNG 帧解码
 * 
 * Web 平台的 Compose Multiplatform 使用 Skiko 进行渲染，
 * 因此可以直接使用 Skia 的 Image API 进行图像解码
 */
class WebFrameDecoder : FrameDecoder {
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
    return WebFrameDecoder()
}
