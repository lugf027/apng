package io.github.lugf027.apng.core

import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger
import okio.BufferedSource

/**
 * APNG 文件加载器
 * 支持从字节数组、文件路径等加载 APNG
 */
class ApngLoader {
    private val parser = ApngParser()

    /**
     * 从字节数组加载 APNG
     */
    fun loadFromBytes(data: ByteArray): ApngImage {
        ApngLogger.d(ApngLogTags.LOADER, "Loading APNG from bytes, size: ${data.size}")
        return try {
            val image = parser.parse(data)
            ApngLogger.i(ApngLogTags.LOADER) { "Successfully loaded APNG: ${image.width}x${image.height}, ${image.numFrames} frames" }
            image
        } catch (e: Exception) {
            ApngLogger.e(ApngLogTags.LOADER, "Failed to load APNG from bytes", e)
            throw e
        }
    }

    /**
     * 从 BufferedSource 加载 APNG
     */
    fun loadFromSource(source: BufferedSource): ApngImage {
        ApngLogger.d(ApngLogTags.LOADER, "Loading APNG from BufferedSource")
        return try {
            val image = parser.parse(source)
            ApngLogger.i(ApngLogTags.LOADER) { "Successfully loaded APNG: ${image.width}x${image.height}, ${image.numFrames} frames" }
            image
        } catch (e: Exception) {
            ApngLogger.e(ApngLogTags.LOADER, "Failed to load APNG from source", e)
            throw e
        }
    }

    /**
     * 平台特定的文件加载由各平台实现
     */
}
