package io.github.lugf027.apng.core

/**
 * acTL (Animation Control) chunk 解析
 * APNG 动画控制信息：帧数和循环次数
 */
data class ActlChunk(
    val numFrames: Int,
    val numPlays: Int
) {
    companion object {
        const val SIZE = 8

        fun parse(data: ByteArray): ActlChunk {
            if (data.size < SIZE) {
                throw InvalidChunkException("acTL chunk data too short")
            }

            val numFrames = bytesToInt(data, 0)
            val numPlays = bytesToInt(data, 4)

            if (numFrames < 1 || numFrames > 0x7FFFFFFF) {
                throw InvalidApngException("Invalid frame count: $numFrames")
            }

            return ActlChunk(
                numFrames = numFrames,
                numPlays = numPlays
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
