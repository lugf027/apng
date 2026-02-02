package io.github.lugf027.apng.core

/**
 * 帧解码器接口
 * 各平台实现具体的图像解码逻辑
 */
expect interface FrameDecoder {
    /**
     * 解码帧数据为平台特定的图像格式
     */
    suspend fun decodeFrame(frame: ApngFrame, imageWidth: Int, imageHeight: Int): Any

    /**
     * 释放资源
     */
    fun release()
}

/**
 * 创建平台特定的帧解码器
 */
expect fun createFrameDecoder(): FrameDecoder

/**
 * 帧解码器工厂
 */
object FrameDecoderFactory {
    fun createDecoder(): FrameDecoder {
        return createFrameDecoder()
    }
}
