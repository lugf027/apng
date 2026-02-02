package io.github.lugf027.apng.core

/**
 * fcTL (Frame Control) chunk 解析
 * 每帧的控制信息：延迟、显示位置、合成操作等
 */
data class FctlChunk(
    val sequenceNumber: Int,
    val width: Int,
    val height: Int,
    val offsetX: Int,
    val offsetY: Int,
    val delayNum: Int,
    val delayDen: Int,
    val disposeOp: Int,
    val blendOp: Int
) {
    companion object {
        const val SIZE = 26

        // Dispose 操作类型
        const val DISPOSE_OP_NONE = 0
        const val DISPOSE_OP_BACKGROUND = 1
        const val DISPOSE_OP_PREVIOUS = 2

        // Blend 操作类型
        const val BLEND_OP_SOURCE = 0
        const val BLEND_OP_OVER = 1

        fun parse(data: ByteArray): FctlChunk {
            if (data.size < SIZE) {
                throw InvalidChunkException("fcTL chunk data too short")
            }

            val sequenceNumber = bytesToInt(data, 0)
            val width = bytesToInt(data, 4)
            val height = bytesToInt(data, 8)
            val offsetX = bytesToInt(data, 12)
            val offsetY = bytesToInt(data, 16)
            val delayNum = bytesToShort(data, 20)
            val delayDen = bytesToShort(data, 22)
            val disposeOp = data[24].toInt() and 0xFF
            val blendOp = data[25].toInt() and 0xFF

            if (width < 1 || height < 1) {
                throw InvalidApngException("Invalid frame dimensions: $width x $height")
            }

            if (delayDen == 0) {
                throw InvalidApngException("Invalid frame delay denominator")
            }

            return FctlChunk(
                sequenceNumber = sequenceNumber,
                width = width,
                height = height,
                offsetX = offsetX,
                offsetY = offsetY,
                delayNum = delayNum,
                delayDen = delayDen,
                disposeOp = disposeOp,
                blendOp = blendOp
            )
        }

        private fun bytesToInt(bytes: ByteArray, offset: Int): Int {
            return ((bytes[offset].toInt() and 0xFF) shl 24) or
                    ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                    (bytes[offset + 3].toInt() and 0xFF)
        }

        private fun bytesToShort(bytes: ByteArray, offset: Int): Int {
            return ((bytes[offset].toInt() and 0xFF) shl 8) or
                    (bytes[offset + 1].toInt() and 0xFF)
        }
    }
}
