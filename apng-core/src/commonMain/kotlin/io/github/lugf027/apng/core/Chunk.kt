package io.github.lugf027.apng.core

import okio.Buffer

/**
 * PNG Chunk 结构定义
 * 每个 chunk 包含：长度(4字节) + 类型(4字节) + 数据(可变) + CRC(4字节)
 */
data class Chunk(
    val type: String,
    val data: ByteArray,
    val crc: UInt
) {
    val length: Int
        get() = data.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chunk) return false

        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false
        if (crc != other.crc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + crc.hashCode()
        return result
    }

    companion object {
        const val HEADER_SIZE = 8 // 长度 4 + 类型 4
        const val CRC_SIZE = 4
        const val MIN_SIZE = HEADER_SIZE + CRC_SIZE

        // 标准 PNG chunk 类型
        const val IHDR = "IHDR"  // 图像头
        const val PLTE = "PLTE"  // 调色板
        const val IDAT = "IDAT"  // 图像数据
        const val IEND = "IEND"  // 图像结束
        const val tRNS = "tRNS"  // 透明度
        const val gAMA = "gAMA"  // 伽玛值

        // APNG 扩展 chunk 类型
        const val acTL = "acTL"  // 动画控制
        const val fcTL = "fcTL"  // 帧控制
        const val fdAT = "fdAT"  // 帧数据

        fun fromBytes(buffer: Buffer): Chunk? {
            if (buffer.size < MIN_SIZE) return null

            val lengthBytes = buffer.readByteArray(4)
            val length = bytesToInt(lengthBytes)

            if (buffer.size < 4 + length + 4) return null

            val typeBytes = buffer.readByteArray(4)
            val type = typeBytes.decodeToString()

            val data = buffer.readByteArray(length.toLong())

            val crcBytes = buffer.readByteArray(4)
            val crc = bytesToUInt(crcBytes)

            return Chunk(type, data, crc)
        }

        private fun bytesToInt(bytes: ByteArray): Int {
            return ((bytes[0].toInt() and 0xFF) shl 24) or
                    ((bytes[1].toInt() and 0xFF) shl 16) or
                    ((bytes[2].toInt() and 0xFF) shl 8) or
                    (bytes[3].toInt() and 0xFF)
        }

        private fun bytesToUInt(bytes: ByteArray): UInt {
            return (((bytes[0].toInt() and 0xFF) shl 24) or
                    ((bytes[1].toInt() and 0xFF) shl 16) or
                    ((bytes[2].toInt() and 0xFF) shl 8) or
                    (bytes[3].toInt() and 0xFF)).toUInt()
        }
    }
}
