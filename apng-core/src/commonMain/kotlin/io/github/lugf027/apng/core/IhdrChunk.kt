package io.github.lugf027.apng.core

/**
 * IHDR (Image Header) chunk 解析
 * 包含图像宽度、高度、位深、颜色类型等信息
 */
data class IhdrChunk(
    val width: Int,
    val height: Int,
    val bitDepth: Int,
    val colorType: Int,
    val compressionMethod: Int,
    val filterMethod: Int,
    val interlaceMethod: Int
) {
    companion object {
        const val SIZE = 13

        // 颜色类型常量
        const val COLOR_TYPE_GRAYSCALE = 0
        const val COLOR_TYPE_RGB = 2
        const val COLOR_TYPE_INDEXED = 3
        const val COLOR_TYPE_GRAYSCALE_ALPHA = 4
        const val COLOR_TYPE_RGBA = 6

        fun parse(data: ByteArray): IhdrChunk {
            if (data.size < SIZE) {
                throw InvalidChunkException("IHDR chunk data too short")
            }

            val width = bytesToInt(data, 0)
            val height = bytesToInt(data, 4)
            val bitDepth = data[8].toInt() and 0xFF
            val colorType = data[9].toInt() and 0xFF
            val compressionMethod = data[10].toInt() and 0xFF
            val filterMethod = data[11].toInt() and 0xFF
            val interlaceMethod = data[12].toInt() and 0xFF

            if (width <= 0 || height <= 0) {
                throw InvalidChunkException("Invalid image dimensions: $width x $height")
            }

            return IhdrChunk(
                width = width,
                height = height,
                bitDepth = bitDepth,
                colorType = colorType,
                compressionMethod = compressionMethod,
                filterMethod = filterMethod,
                interlaceMethod = interlaceMethod
            )
        }

        private fun bytesToInt(bytes: ByteArray, offset: Int): Int {
            return ((bytes[offset].toInt() and 0xFF) shl 24) or
                    ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                    (bytes[offset + 3].toInt() and 0xFF)
        }
    }
}
