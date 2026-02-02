package io.github.lugf027.apng.core

/**
 * PNG 文件签名定义
 * PNG 文件以特定的 8 字节签名开始
 */
object PngSignature {
    val BYTES = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)
    const val SIZE = 8

    fun isValid(bytes: ByteArray): Boolean {
        if (bytes.size < SIZE) return false
        return bytes.sliceArray(0 until SIZE).contentEquals(BYTES)
    }
}
