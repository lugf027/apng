package io.github.lugf027.apng.internal.apng

import okio.Buffer
import okio.BufferedSource

internal object PngConstants {
    val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    const val CHUNK_IHDR = 0x49484452 // "IHDR"
    const val CHUNK_IDAT = 0x49444154 // "IDAT"
    const val CHUNK_IEND = 0x49454E44 // "IEND"
    const val CHUNK_acTL = 0x6163544C // "acTL"
    const val CHUNK_fcTL = 0x6663544C // "fcTL"
    const val CHUNK_fdAT = 0x66644154 // "fdAT"

    // Auxiliary chunks to preserve
    const val CHUNK_gAMA = 0x67414D41
    const val CHUNK_cHRM = 0x6348524D
    const val CHUNK_sRGB = 0x73524742
    const val CHUNK_iCCP = 0x69434350
    const val CHUNK_sBIT = 0x73424954
    const val CHUNK_PLTE = 0x504C5445
    const val CHUNK_tRNS = 0x74524E53
    const val CHUNK_bKGD = 0x624B4744
    const val CHUNK_pHYs = 0x70485973

    val AUXILIARY_CHUNK_TYPES = setOf(
        CHUNK_gAMA, CHUNK_cHRM, CHUNK_sRGB, CHUNK_iCCP,
        CHUNK_sBIT, CHUNK_PLTE, CHUNK_tRNS, CHUNK_bKGD, CHUNK_pHYs
    )
}

internal object PngChunkReader {

    fun readPngSignature(source: BufferedSource) {
        val signature = source.readByteArray(8)
        if (!signature.contentEquals(PngConstants.PNG_SIGNATURE)) {
            throw ApngParseException("Invalid PNG signature")
        }
    }

    fun readChunk(source: BufferedSource): PngChunk {
        val length = source.readInt()
        val type = source.readInt()
        val data = if (length > 0) source.readByteArray(length.toLong()) else ByteArray(0)
        val crc = source.readInt()
        return PngChunk(type, data, crc)
    }

    fun writeChunk(buffer: Buffer, type: Int, data: ByteArray) {
        buffer.writeInt(data.size)
        buffer.writeInt(type)
        buffer.write(data)
        val crc = computeCrc(type, data)
        buffer.writeInt(crc)
    }

    fun writePngSignature(buffer: Buffer) {
        buffer.write(PngConstants.PNG_SIGNATURE)
    }

    private fun computeCrc(type: Int, data: ByteArray): Int {
        var crc = 0xFFFFFFFFL.toInt()
        // CRC over type bytes
        crc = updateCrc(crc, (type shr 24 and 0xFF).toByte())
        crc = updateCrc(crc, (type shr 16 and 0xFF).toByte())
        crc = updateCrc(crc, (type shr 8 and 0xFF).toByte())
        crc = updateCrc(crc, (type and 0xFF).toByte())
        // CRC over data bytes
        for (b in data) {
            crc = updateCrc(crc, b)
        }
        return crc xor 0xFFFFFFFFL.toInt()
    }

    private fun updateCrc(crc: Int, b: Byte): Int {
        return CRC_TABLE[(crc xor b.toInt()) and 0xFF] xor (crc ushr 8)
    }

    private val CRC_TABLE = IntArray(256).also { table ->
        for (n in 0 until 256) {
            var c = n
            for (k in 0 until 8) {
                c = if (c and 1 != 0) {
                    0xEDB88320.toInt() xor (c ushr 1)
                } else {
                    c ushr 1
                }
            }
            table[n] = c
        }
    }
}

internal class ApngParseException(message: String) : Exception(message)
