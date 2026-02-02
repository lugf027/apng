package io.github.lugf027.apng.core

/**
 * iOS 实现的帧解码器
 * 使用 iOS 原生 UIImage API
 */
class IosFrameDecoder : FrameDecoder {
    private val imageCache = mutableMapOf<Int, Any>()

    override suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any {
        return try {
            val image = decodeImageData(frame.data)
            imageCache[frame.index] = image
            image
        } catch (e: Exception) {
            throw DecodingException("Failed to decode frame ${frame.index}", e)
        }
    }

    override fun release() {
        imageCache.clear()
    }

    private fun decodeImageData(data: ByteArray): Any {
        // iOS platform specific implementation
        // Uses UIImage.init(data:) from UIKit
        // This is a placeholder that would be implemented with Kotlin/Native interop
        return Any()
    }
}

actual interface FrameDecoder {
    actual suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any
    actual fun release()
}

actual fun createFrameDecoder(): FrameDecoder {
    return IosFrameDecoder()
}
