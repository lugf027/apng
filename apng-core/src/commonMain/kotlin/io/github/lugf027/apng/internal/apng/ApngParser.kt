package io.github.lugf027.apng.internal.apng

import okio.Buffer

internal object ApngParser {

    fun parse(bytes: ByteArray): ApngAnimationData {
        val source = Buffer().apply { write(bytes) }

        PngChunkReader.readPngSignature(source)

        var ihdrData: ByteArray? = null
        var canvasWidth = 0
        var canvasHeight = 0
        var numPlays = 0
        var numFrames = 1
        var hasActl = false

        val auxiliaryChunks = mutableListOf<PngChunk>()
        val allChunks = mutableListOf<PngChunk>()

        // First pass: read all chunks
        while (!source.exhausted()) {
            val chunk = PngChunkReader.readChunk(source)
            allChunks.add(chunk)
            if (chunk.type == PngConstants.CHUNK_IEND) break
        }
        // Process chunks
        val frames = mutableListOf<RawApngFrame>()
        var currentFctl: FctlData? = null
        val idatDataChunks = mutableListOf<ByteArray>()
        var firstFrameIsDefault = false

        for (chunk in allChunks) {
            when (chunk.type) {
                PngConstants.CHUNK_IHDR -> {
                    ihdrData = chunk.data
                    val ihdrBuf = Buffer().apply { write(chunk.data) }
                    canvasWidth = ihdrBuf.readInt()
                    canvasHeight = ihdrBuf.readInt()
                }

                PngConstants.CHUNK_acTL -> {
                    hasActl = true
                    val actlBuf = Buffer().apply { write(chunk.data) }
                    numFrames = actlBuf.readInt()
                    numPlays = actlBuf.readInt()
                }

                PngConstants.CHUNK_fcTL -> {
                    // If we had a previous fcTL with accumulated data, build that frame
                    if (currentFctl != null && idatDataChunks.isNotEmpty()) {
                        val isIdat = firstFrameIsDefault && frames.isEmpty()
                        val frame = buildFrame(
                            currentFctl, idatDataChunks, ihdrData!!,
                            auxiliaryChunks, isIdat = isIdat
                        )
                        frames.add(frame)
                        idatDataChunks.clear()
                    }
                    currentFctl = parseFctl(chunk.data)
                    if (frames.isEmpty() && idatDataChunks.isEmpty()) {
                        firstFrameIsDefault = true
                    }
                }

                PngConstants.CHUNK_IDAT -> {
                    idatDataChunks.add(chunk.data)
                }

                PngConstants.CHUNK_fdAT -> {
                    // fdAT data: first 4 bytes = sequence number, rest = IDAT equivalent data
                    val frameData = chunk.data.copyOfRange(4, chunk.data.size)
                    if (currentFctl != null) {
                        idatDataChunks.add(frameData)
                    }
                }

                PngConstants.CHUNK_IEND -> {
                    // Build the last frame if pending
                    if (currentFctl != null && idatDataChunks.isNotEmpty()) {
                        val isIdat = firstFrameIsDefault && frames.isEmpty()
                        val frame = buildFrame(
                            currentFctl, idatDataChunks, ihdrData!!,
                            auxiliaryChunks, isIdat = isIdat
                        )
                        frames.add(frame)
                    }
                }

                else -> {
                    if (chunk.type in PngConstants.AUXILIARY_CHUNK_TYPES) {
                        auxiliaryChunks.add(chunk)
                    }
                }
            }
        }

        // If no acTL found, this is a static PNG - create single frame from IDAT
        if (!hasActl || frames.isEmpty()) {
            val singleFrame = buildStaticFrame(
                allChunks, ihdrData!!, canvasWidth, canvasHeight, auxiliaryChunks
            )
            return ApngAnimationData(
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                numPlays = 1,
                numFrames = 1,
                frames = listOf(singleFrame),
                ihdrData = ihdrData,
                auxiliaryChunks = auxiliaryChunks
            )
        }

        return ApngAnimationData(
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            numPlays = numPlays,
            numFrames = numFrames,
            frames = frames,
            ihdrData = ihdrData!!,
            auxiliaryChunks = auxiliaryChunks
        )
    }

    private fun buildFrame(
        fctl: FctlData,
        idatDataChunks: List<ByteArray>,
        originalIhdr: ByteArray,
        auxiliaryChunks: List<PngChunk>,
        isIdat: Boolean
    ): RawApngFrame {
        val pngBytes = rebuildPng(
            fctl, idatDataChunks, originalIhdr, auxiliaryChunks, isIdat
        )
        return RawApngFrame(
            pngBytes = pngBytes,
            xOffset = fctl.xOffset,
            yOffset = fctl.yOffset,
            width = fctl.width,
            height = fctl.height,
            delayNum = fctl.delayNum,
            delayDen = fctl.delayDen,
            disposeOp = fctl.disposeOp,
            blendOp = fctl.blendOp
        )
    }

    private fun buildStaticFrame(
        allChunks: List<PngChunk>,
        ihdrData: ByteArray,
        width: Int,
        height: Int,
        auxiliaryChunks: List<PngChunk>
    ): RawApngFrame {
        val buffer = Buffer()
        PngChunkReader.writePngSignature(buffer)
        PngChunkReader.writeChunk(buffer, PngConstants.CHUNK_IHDR, ihdrData)
        for (aux in auxiliaryChunks) {
            PngChunkReader.writeChunk(buffer, aux.type, aux.data)
        }
        for (chunk in allChunks) {
            if (chunk.type == PngConstants.CHUNK_IDAT) {
                PngChunkReader.writeChunk(buffer, PngConstants.CHUNK_IDAT, chunk.data)
            }
        }
        PngChunkReader.writeChunk(buffer, PngConstants.CHUNK_IEND, ByteArray(0))

        return RawApngFrame(
            pngBytes = buffer.readByteArray(),
            xOffset = 0,
            yOffset = 0,
            width = width,
            height = height,
            delayNum = 0,
            delayDen = 100,
            disposeOp = DisposeOp.NONE,
            blendOp = BlendOp.SOURCE
        )
    }

    private fun rebuildPng(
        fctl: FctlData,
        idatDataChunks: List<ByteArray>,
        originalIhdr: ByteArray,
        auxiliaryChunks: List<PngChunk>,
        isIdat: Boolean
    ): ByteArray {
        val buffer = Buffer()
        PngChunkReader.writePngSignature(buffer)

        // Build frame-specific IHDR with frame dimensions
        val ihdr: ByteArray
        if (isIdat) {
            // Default image frame uses original IHDR
            ihdr = originalIhdr
        } else {
            // Sub-frame: modify IHDR with frame width/height
            ihdr = originalIhdr.copyOf()
            val ihdrBuf = Buffer()
            ihdrBuf.writeInt(fctl.width)
            ihdrBuf.writeInt(fctl.height)
            val whBytes = ihdrBuf.readByteArray()
            whBytes.copyInto(ihdr, 0, 0, 8)
        }
        PngChunkReader.writeChunk(buffer, PngConstants.CHUNK_IHDR, ihdr)

        // Write auxiliary chunks
        for (aux in auxiliaryChunks) {
            PngChunkReader.writeChunk(buffer, aux.type, aux.data)
        }

        // Write IDAT chunks
        for (data in idatDataChunks) {
            PngChunkReader.writeChunk(buffer, PngConstants.CHUNK_IDAT, data)
        }

        // Write IEND
        PngChunkReader.writeChunk(buffer, PngConstants.CHUNK_IEND, ByteArray(0))

        return buffer.readByteArray()
    }

    private fun parseFctl(data: ByteArray): FctlData {
        val buf = Buffer().apply { write(data) }
        val sequenceNumber = buf.readInt()
        val width = buf.readInt()
        val height = buf.readInt()
        val xOffset = buf.readInt()
        val yOffset = buf.readInt()
        val delayNum = buf.readShort().toInt() and 0xFFFF
        val delayDen = buf.readShort().toInt() and 0xFFFF
        val disposeOp = DisposeOp.fromValue(buf.readByte())
        val blendOp = BlendOp.fromValue(buf.readByte())
        return FctlData(
            sequenceNumber, width, height, xOffset, yOffset,
            delayNum, delayDen, disposeOp, blendOp
        )
    }

    private data class FctlData(
        val sequenceNumber: Int,
        val width: Int,
        val height: Int,
        val xOffset: Int,
        val yOffset: Int,
        val delayNum: Int,
        val delayDen: Int,
        val disposeOp: DisposeOp,
        val blendOp: BlendOp
    )
}
