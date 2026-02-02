package io.github.lugf027.apng.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect

/**
 * Desktop 平台的 APNG 合成数据加载实现
 * 使用 Skia 进行 APNG 帧解析和图像处理
 */
internal actual fun loadApngComposition(data: ByteArray): ApngComposition {
    // 检查 PNG 签名
    if (data.size < 8 || !isPngSignature(data)) {
        throw IllegalArgumentException("Invalid PNG data")
    }
    
    // 解析 APNG 结构
    val parseResult = parseApngStructure(data)
    
    if (!parseResult.hasActl || parseResult.frameInfoList.isEmpty()) {
        // 不是 APNG，作为静态图像处理
        val image = Image.makeFromEncoded(data)
        
        val bitmap = image.toComposeImageBitmap()
        image.close()
        
        return ApngComposition(
            width = bitmap.width,
            height = bitmap.height,
            frames = listOf(ApngFrame(
                bitmap = bitmap,
                delayMs = 0,
                width = bitmap.width,
                height = bitmap.height
            )),
            loopCount = 1,
            isAnimated = false
        )
    }
    
    // 构建每帧的完整图像
    val frames = buildFrames(
        parseResult.ihdrData!!,
        parseResult.commonChunks,
        parseResult.frameInfoList,
        parseResult.globalWidth,
        parseResult.globalHeight
    )
    
    return ApngComposition(
        width = parseResult.globalWidth,
        height = parseResult.globalHeight,
        frames = frames,
        loopCount = parseResult.loopCount,
        isAnimated = true
    )
}

/**
 * APNG 解析结果
 */
private data class ApngParseResult(
    val globalWidth: Int,
    val globalHeight: Int,
    val ihdrData: ByteArray?,
    val commonChunks: List<ByteArray>,
    val frameInfoList: List<FrameInfo>,
    val hasActl: Boolean,
    val loopCount: Int
)

/**
 * 帧信息
 */
private data class FrameInfo(
    val width: Int,
    val height: Int,
    val offsetX: Int,
    val offsetY: Int,
    val delayNum: Int,
    val delayDen: Int,
    val disposeOp: Int,
    val blendOp: Int,
    val idatData: MutableList<ByteArray> = mutableListOf()
) {
    val delayMs: Long get() {
        val den = if (delayDen == 0) 1000 else delayDen
        return (delayNum.toLong() * 1000) / den
    }
}

/**
 * 解析 APNG 结构
 */
private fun parseApngStructure(data: ByteArray): ApngParseResult {
    val commonChunks = mutableListOf<ByteArray>()
    var ihdrData: ByteArray? = null
    var globalWidth = 0
    var globalHeight = 0
    var hasActl = false
    var loopCount = 0
    
    val frameInfoList = mutableListOf<FrameInfo>()
    var currentFrameInfo: FrameInfo? = null
    
    var offset = 8
    while (offset + 8 <= data.size) {
        val length = bytesToInt(data, offset)
        if (offset + 12 + length > data.size) break
        
        val typeBytes = data.sliceArray(offset + 4 until offset + 8)
        val type = String(typeBytes)
        val chunkData = if (length > 0) data.sliceArray(offset + 8 until offset + 8 + length) else ByteArray(0)
        
        when (type) {
            "IHDR" -> {
                ihdrData = chunkData
                globalWidth = bytesToInt(chunkData, 0)
                globalHeight = bytesToInt(chunkData, 4)
            }
            "acTL" -> {
                hasActl = true
                if (chunkData.size >= 8) {
                    loopCount = bytesToInt(chunkData, 4)
                }
            }
            "PLTE", "tRNS", "cHRM", "gAMA", "iCCP", "sBIT", "sRGB", "pHYs", "bKGD" -> {
                val fullChunk = data.sliceArray(offset until offset + 12 + length)
                commonChunks.add(fullChunk)
            }
            "fcTL" -> {
                // 保存上一帧
                currentFrameInfo?.let { frame ->
                    if (frame.idatData.isNotEmpty()) {
                        frameInfoList.add(frame)
                    }
                }
                
                // 解析新帧控制
                if (chunkData.size >= 26) {
                    val width = bytesToInt(chunkData, 4)
                    val height = bytesToInt(chunkData, 8)
                    val offsetX = bytesToInt(chunkData, 12)
                    val offsetY = bytesToInt(chunkData, 16)
                    val delayNum = bytesToShort(chunkData, 20)
                    val delayDen = bytesToShort(chunkData, 22)
                    val disposeOp = chunkData[24].toInt() and 0xFF
                    val blendOp = chunkData[25].toInt() and 0xFF
                    
                    currentFrameInfo = FrameInfo(
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
            }
            "IDAT" -> {
                currentFrameInfo?.idatData?.add(chunkData)
                    ?: run {
                        currentFrameInfo = FrameInfo(
                            width = globalWidth,
                            height = globalHeight,
                            offsetX = 0,
                            offsetY = 0,
                            delayNum = 100,
                            delayDen = 1000,
                            disposeOp = 0,
                            blendOp = 0
                        )
                        currentFrameInfo.idatData.add(chunkData)
                    }
            }
            "fdAT" -> {
                if (chunkData.size > 4) {
                    val frameData = chunkData.sliceArray(4 until chunkData.size)
                    currentFrameInfo?.idatData?.add(frameData)
                }
            }
            "IEND" -> {
                currentFrameInfo?.let { frame ->
                    if (frame.idatData.isNotEmpty()) {
                        frameInfoList.add(frame)
                    }
                }
                break
            }
        }
        
        offset += 12 + length
    }
    
    return ApngParseResult(
        globalWidth = globalWidth,
        globalHeight = globalHeight,
        ihdrData = ihdrData,
        commonChunks = commonChunks,
        frameInfoList = frameInfoList,
        hasActl = hasActl,
        loopCount = loopCount
    )
}

/**
 * 构建所有帧的 ImageBitmap
 */
private fun buildFrames(
    ihdrData: ByteArray,
    commonChunks: List<ByteArray>,
    frameInfoList: List<FrameInfo>,
    globalWidth: Int,
    globalHeight: Int
): List<ApngFrame> {
    val frames = mutableListOf<ApngFrame>()
    
    // 创建画布用于合成帧
    val canvasBitmap = Bitmap()
    canvasBitmap.allocPixels(ImageInfo.makeN32(globalWidth, globalHeight, ColorAlphaType.PREMUL))
    val canvas = Canvas(canvasBitmap)
    canvas.clear(0x00000000) // 透明背景
    
    var previousBitmap: Bitmap? = null
    
    for (frameInfo in frameInfoList) {
        try {
            // 构建这一帧的 PNG 数据
            val framePng = buildFramePng(ihdrData, commonChunks, frameInfo)
            val frameImage = Image.makeFromEncoded(framePng)
            
            // 根据 dispose_op 处理
            when (frameInfo.disposeOp) {
                1, 2 -> { // APNG_DISPOSE_OP_BACKGROUND / PREVIOUS
                    previousBitmap = canvasBitmap.copy()
                }
            }
            
            // 根据 blend_op 绘制
            if (frameInfo.blendOp == 0) {
                // APNG_BLEND_OP_SOURCE - 替换
                canvas.save()
                canvas.clipRect(Rect.makeXYWH(
                    frameInfo.offsetX.toFloat(),
                    frameInfo.offsetY.toFloat(),
                    frameInfo.width.toFloat(),
                    frameInfo.height.toFloat()
                ))
                canvas.clear(0x00000000)
                canvas.restore()
            }
            
            // 绘制帧图像
            canvas.drawImageRect(
                frameImage,
                Rect.makeWH(frameImage.width.toFloat(), frameImage.height.toFloat()),
                Rect.makeXYWH(
                    frameInfo.offsetX.toFloat(),
                    frameInfo.offsetY.toFloat(),
                    frameInfo.width.toFloat(),
                    frameInfo.height.toFloat()
                ),
                Paint()
            )
            
            // 创建当前帧的完整图像
            val resultBitmap = canvasBitmap.copy()
            val resultImage = Image.makeFromBitmap(resultBitmap)
            val composeBitmap = resultImage.toComposeImageBitmap()
            resultImage.close()
            resultBitmap.close()
            
            frames.add(ApngFrame(
                bitmap = composeBitmap,
                delayMs = frameInfo.delayMs.coerceAtLeast(16L),
                offsetX = frameInfo.offsetX,
                offsetY = frameInfo.offsetY,
                width = frameInfo.width,
                height = frameInfo.height,
                disposeOp = frameInfo.disposeOp,
                blendOp = frameInfo.blendOp
            ))
            
            frameImage.close()
            
            // 处理 dispose_op
            when (frameInfo.disposeOp) {
                1 -> { // APNG_DISPOSE_OP_BACKGROUND
                    canvas.save()
                    canvas.clipRect(Rect.makeXYWH(
                        frameInfo.offsetX.toFloat(),
                        frameInfo.offsetY.toFloat(),
                        frameInfo.width.toFloat(),
                        frameInfo.height.toFloat()
                    ))
                    canvas.clear(0x00000000)
                    canvas.restore()
                    previousBitmap?.close()
                    previousBitmap = null
                }
                2 -> { // APNG_DISPOSE_OP_PREVIOUS
                    previousBitmap?.let { prev ->
                        canvas.clear(0x00000000)
                        val prevImage = Image.makeFromBitmap(prev)
                        canvas.drawImage(prevImage, 0f, 0f)
                        prevImage.close()
                        prev.close()
                    }
                    previousBitmap = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    canvasBitmap.close()
    previousBitmap?.close()
    
    return frames
}

/**
 * 为单帧构建完整的 PNG 数据
 */
private fun buildFramePng(
    ihdrData: ByteArray,
    commonChunks: List<ByteArray>,
    frameInfo: FrameInfo
): ByteArray {
    val output = mutableListOf<Byte>()
    
    // PNG 签名
    output.addAll(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).toList())
    
    // 创建新的 IHDR
    val newIhdrData = ByteArray(13)
    intToBytes(frameInfo.width).copyInto(newIhdrData, 0)
    intToBytes(frameInfo.height).copyInto(newIhdrData, 4)
    ihdrData.copyInto(newIhdrData, 8, 8, 13)
    output.addAll(createChunk("IHDR", newIhdrData).toList())
    
    // 添加公共 chunks
    for (chunk in commonChunks) {
        output.addAll(chunk.toList())
    }
    
    // 合并并添加 IDAT chunks
    val combinedIdatData = mutableListOf<Byte>()
    for (idatChunk in frameInfo.idatData) {
        combinedIdatData.addAll(idatChunk.toList())
    }
    output.addAll(createChunk("IDAT", combinedIdatData.toByteArray()).toList())
    
    // 添加 IEND
    output.addAll(createChunk("IEND", ByteArray(0)).toList())
    
    return output.toByteArray()
}

private fun Bitmap.copy(): Bitmap {
    val copy = Bitmap()
    copy.allocPixels(this.imageInfo)
    val pixels = this.readPixels(this.imageInfo, 0, 0)
    if (pixels != null) {
        copy.installPixels(pixels)
    }
    return copy
}

private fun isPngSignature(data: ByteArray): Boolean {
    val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    for (i in signature.indices) {
        if (data[i] != signature[i]) return false
    }
    return true
}

private fun createChunk(type: String, data: ByteArray): ByteArray {
    val typeBytes = type.toByteArray(Charsets.US_ASCII)
    val crcInput = typeBytes + data
    val crc = calculateCrc(crcInput)
    
    val result = ByteArray(4 + 4 + data.size + 4)
    intToBytes(data.size).copyInto(result, 0)
    typeBytes.copyInto(result, 4)
    data.copyInto(result, 8)
    intToBytes(crc).copyInto(result, 8 + data.size)
    
    return result
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

private fun intToBytes(value: Int): ByteArray {
    return byteArrayOf(
        ((value shr 24) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        (value and 0xFF).toByte()
    )
}

private fun calculateCrc(data: ByteArray): Int {
    var crc = 0xFFFFFFFF.toInt()
    for (byte in data) {
        crc = crc xor (byte.toInt() and 0xFF)
        for (j in 0 until 8) {
            crc = if ((crc and 1) != 0) {
                (crc ushr 1) xor 0xEDB88320.toInt()
            } else {
                crc ushr 1
            }
        }
    }
    return crc xor 0xFFFFFFFF.toInt()
}
