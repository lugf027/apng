package io.github.lugf027.apng.core

import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger
import okio.Buffer
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

/**
 * APNG 文件解析器
 * 支持 PNG 和 APNG 格式的读取和解析
 */
class ApngParser {
    fun parse(source: BufferedSource): ApngImage {
        ApngLogger.d(ApngLogTags.PARSER, "Starting to parse APNG from BufferedSource")
        val buffer = Buffer()
        buffer.writeAll(source)
        
        return parse(buffer.readByteArray())
    }

    fun parse(data: ByteArray): ApngImage {
        ApngLogger.d(ApngLogTags.PARSER, "Starting to parse APNG data, size: ${data.size} bytes")
        
        if (!PngSignature.isValid(data)) {
            ApngLogger.e(ApngLogTags.PARSER, "Invalid PNG signature")
            throw InvalidPngSignatureException()
        }
        ApngLogger.v(ApngLogTags.PARSER, "PNG signature validated")

        var offset = PngSignature.SIZE
        var ihdr: IhdrChunk? = null
        var actl: ActlChunk? = null
        val frames = mutableListOf<ApngFrame>()
        val idatChunks = mutableListOf<ByteArray>()
        val fdatChunks = mutableListOf<Pair<Int, ByteArray>>()  // sequence number to data
        var defaultImageData: ByteArray? = null
        var seqNumber = 0

        while (offset < data.size) {
            // 读取 chunk 长度
            if (offset + 8 > data.size) break

            val length = bytesToInt(data, offset)
            offset += 4

            // 读取 chunk 类型
            if (offset + 4 > data.size) break
            val typeBytes = data.sliceArray(offset until offset + 4)
            val type = typeBytes.decodeToString()
            offset += 4

            // 检查数据是否完整
            if (offset + length + 4 > data.size) {
                ApngLogger.e(ApngLogTags.PARSER, "Incomplete chunk data for type: $type")
                throw InvalidChunkException("Incomplete chunk data")
            }

            val chunkData = data.sliceArray(offset until offset + length)
            offset += length

            // 跳过 CRC
            offset += 4

            try {
                when (type) {
                    Chunk.IHDR -> {
                        ihdr = IhdrChunk.parse(chunkData)
                        ApngLogger.v(ApngLogTags.CHUNK) { "Parsed IHDR: ${ihdr.width}x${ihdr.height}, bitDepth=${ihdr.bitDepth}, colorType=${ihdr.colorType}" }
                    }
                    Chunk.acTL -> {
                        actl = ActlChunk.parse(chunkData)
                        ApngLogger.v(ApngLogTags.CHUNK) { "Parsed acTL: numFrames=${actl.numFrames}, numPlays=${actl.numPlays}" }
                    }
                    Chunk.fcTL -> {
                        val fctl = FctlChunk.parse(chunkData)
                        seqNumber = fctl.sequenceNumber
                        ApngLogger.v(ApngLogTags.CHUNK) { "Parsed fcTL: seq=$seqNumber, ${fctl.width}x${fctl.height}, delay=${fctl.delayNum}/${fctl.delayDen}" }
                    }
                    Chunk.IDAT -> {
                        idatChunks.add(chunkData)
                        ApngLogger.v(ApngLogTags.CHUNK) { "Parsed IDAT chunk, size: ${chunkData.size} bytes" }
                    }
                    Chunk.fdAT -> {
                        if (chunkData.size < 4) {
                            ApngLogger.e(ApngLogTags.PARSER, "fdAT chunk too short: ${chunkData.size} bytes")
                            throw InvalidChunkException("fdAT chunk too short")
                        }
                        val dataSeqNum = bytesToInt(chunkData, 0)
                        val frameData = chunkData.sliceArray(4 until chunkData.size)
                        fdatChunks.add(dataSeqNum to frameData)
                        ApngLogger.v(ApngLogTags.CHUNK) { "Parsed fdAT chunk, seq=$dataSeqNum, size: ${frameData.size} bytes" }
                    }
                    Chunk.IEND -> {
                        ApngLogger.v(ApngLogTags.CHUNK, "Reached IEND chunk")
                        break
                    }
                }
            } catch (e: Exception) {
                ApngLogger.e(ApngLogTags.PARSER, "Failed to parse chunk $type: ${e.message}", e)
                throw InvalidApngException("Failed to parse chunk $type: ${e.message}")
            }
        }

        if (ihdr == null) {
            ApngLogger.e(ApngLogTags.PARSER, "Missing IHDR chunk")
            throw InvalidApngException("Missing IHDR chunk")
        }

        val isAnimated = actl != null
        val numFrames = actl?.numFrames ?: 1
        ApngLogger.d(ApngLogTags.PARSER) { "Image info: ${ihdr.width}x${ihdr.height}, animated=$isAnimated, numFrames=$numFrames" }

        // 如果有 IDAT，构建默认图像
        if (idatChunks.isNotEmpty()) {
            val buffer = mutableListOf<Byte>()
            for (chunk in idatChunks) {
                buffer.addAll(chunk.toList())
            }
            defaultImageData = buffer.toByteArray()
            ApngLogger.v(ApngLogTags.PARSER) { "Built default image data: ${defaultImageData.size} bytes from ${idatChunks.size} IDAT chunks" }
        }

        // 解析帧数据（简化版本，实际需要处理 fdAT 序列）
        val frameList = mutableListOf<ApngFrame>()
        if (isAnimated && fdatChunks.isNotEmpty()) {
            var frameIndex = 0
            var currentSeqNum = 0
            var currentFrameData = mutableListOf<ByteArray>()
            var fctl: FctlChunk? = null

            for (i in fdatChunks.indices) {
                val (seqNum, data) = fdatChunks[i]
                if (seqNum != currentSeqNum) {
                    if (currentFrameData.isNotEmpty() && fctl != null) {
                        val frameBuffer = mutableListOf<Byte>()
                        for (frameChunk in currentFrameData) {
                            frameBuffer.addAll(frameChunk.toList())
                        }
                        frameList.add(
                            ApngFrame(
                                index = frameIndex++,
                                data = frameBuffer.toByteArray(),
                                delayNum = fctl.delayNum,
                                delayDen = fctl.delayDen,
                                disposeOp = fctl.disposeOp,
                                blendOp = fctl.blendOp,
                                width = fctl.width,
                                height = fctl.height,
                                offsetX = fctl.offsetX,
                                offsetY = fctl.offsetY
                            )
                        )
                        ApngLogger.v(ApngLogTags.FRAME) { "Built frame $frameIndex: ${fctl.width}x${fctl.height}" }
                    }
                    currentFrameData.clear()
                    currentSeqNum = seqNum
                }
                currentFrameData.add(data)
            }

            if (currentFrameData.isNotEmpty() && fctl != null) {
                val frameBuffer = mutableListOf<Byte>()
                for (frameChunk in currentFrameData) {
                    frameBuffer.addAll(frameChunk.toList())
                }
                frameList.add(
                    ApngFrame(
                        index = frameIndex,
                        data = frameBuffer.toByteArray(),
                        delayNum = fctl.delayNum,
                        delayDen = fctl.delayDen,
                        disposeOp = fctl.disposeOp,
                        blendOp = fctl.blendOp,
                        width = fctl.width,
                        height = fctl.height,
                        offsetX = fctl.offsetX,
                        offsetY = fctl.offsetY
                    )
                )
                ApngLogger.v(ApngLogTags.FRAME) { "Built final frame $frameIndex: ${fctl.width}x${fctl.height}" }
            }
        }

        ApngLogger.i(ApngLogTags.PARSER) { "APNG parsing completed: ${ihdr.width}x${ihdr.height}, ${frameList.size} frames" }

        return ApngImage(
            width = ihdr.width,
            height = ihdr.height,
            isAnimated = isAnimated,
            numFrames = numFrames,
            frames = frameList,
            defaultImage = defaultImageData
        )
    }

    private fun bytesToInt(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                (bytes[offset + 3].toInt() and 0xFF)
    }
}
