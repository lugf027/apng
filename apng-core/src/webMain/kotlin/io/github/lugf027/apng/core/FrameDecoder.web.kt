package io.github.lugf027.apng.core

/**
 * Web 实现的帧解码器
 * 使用 HTML5 Canvas/WebGL
 */
class WebFrameDecoder : FrameDecoder {
    private val imageCache = mutableMapOf<Int, Any>()

    override suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any {
        return try {
            val imageData = decodeToImageData(frame.data)
            imageCache[frame.index] = imageData
            imageData
        } catch (e: Exception) {
            throw DecodingException("Failed to decode frame ${frame.index}", e)
        }
    }

    override fun release() {
        imageCache.clear()
    }

    private fun decodeToImageData(data: ByteArray): Any {
        // Web platform specific implementation
        // Uses createImageBitmap or Canvas
        // This is a placeholder for JS interop implementation
        return Any()
    }
}

actual interface FrameDecoder {
    actual suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any
    actual fun release()
}

actual fun createFrameDecoder(): FrameDecoder {
    return WebFrameDecoder()
}
