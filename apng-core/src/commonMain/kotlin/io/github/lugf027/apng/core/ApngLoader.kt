package io.github.lugf027.apng.core

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
        return parser.parse(data)
    }

    /**
     * 从 BufferedSource 加载 APNG
     */
    fun loadFromSource(source: BufferedSource): ApngImage {
        return parser.parse(source)
    }

    /**
     * 平台特定的文件加载由各平台实现
     */
}
