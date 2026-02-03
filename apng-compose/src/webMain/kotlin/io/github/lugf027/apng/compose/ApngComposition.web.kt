package io.github.lugf027.apng.compose

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect

/**
 * Web 平台的 APNG 合成数据加载实现
 * 
 * Web 平台通过 Kotlin/Wasm 编译后，可以使用 Skia 库（通过 Kotlin/Wasm 绑定）
 * 进行 APNG 帧解析和图像处理，与 Desktop 实现保持一致
 */
internal actual fun parseApngCompositionData(data: ByteArray): ApngComposition {
    // 检查 PNG 签名
    if (data.size < 8 || !isPngSignature(data)) {
        throw IllegalArgumentException("Invalid PNG data")
    }
    
    // 解析 APNG 结构
    val parseResult = parseApngStructure(data)
    
    if (!parseResult.hasActl || parseResult.frameInfoList.isEmpty()) {
        // 不是 APNG，作为静态图像处理
        val image = Image.makeFromEncoded(data)
            ?: throw IllegalArgumentException("Failed to decode PNG")
        
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
        data
    )
    
    return ApngComposition(
        width = parseResult.ihdrData.width,
        height = parseResult.ihdrData.height,
        frames = frames,
        loopCount = parseResult.actlData?.numPlays ?: 0,
        isAnimated = true
    )
}

/**
 * 检查 PNG 文件签名
 */
private fun isPngSignature(data: ByteArray): Boolean {
    if (data.size < 8) return false
    val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    return data.take(8).toByteArray().contentEquals(signature)
}

/**
 * 解析 APNG 结构
 */
private fun parseApngStructure(data: ByteArray): ApngParseResult {
    var offset = 8 // 跳过 PNG 签名
    var hasActl = false
    var ihdrData: IhdrData? = null
    var actlData: ActlData? = null
    val frameInfoList = mutableListOf<FrameInfo>()
    val commonChunks = mutableListOf<ByteArray>()
    
    while (offset < data.size) {
        // 读取 chunk 长度
        if (offset + 4 > data.size) break
        val length = (
            (data[offset].toInt() and 0xFF shl 24) or
            (data[offset + 1].toInt() and 0xFF shl 16) or
            (data[offset + 2].toInt() and 0xFF shl 8) or
            (data[offset + 3].toInt() and 0xFF)
        )
        offset += 4
        
        if (offset + 4 + length + 4 > data.size) break
        
        // 读取 chunk 类型
        val chunkType = data.sliceArray(offset until offset + 4).map { it.toInt().toChar() }.joinToString("")
        offset += 4
        
        // 读取 chunk 数据
        val chunkData = data.sliceArray(offset until offset + length)
        offset += length
        
        // 跳过 CRC
        offset += 4
        
        when (chunkType) {
            "IHDR" -> {
                ihdrData = IhdrData(
                    width = readInt(chunkData, 0),
                    height = readInt(chunkData, 4)
                )
            }
            "acTL" -> {
                hasActl = true
                actlData = ActlData(
                    numFrames = readInt(chunkData, 0),
                    numPlays = readInt(chunkData, 4)
                )
            }
            "fcTL" -> {
                frameInfoList.add(FrameInfo(
                    sequenceNumber = readInt(chunkData, 0),
                    width = readInt(chunkData, 4),
                    height = readInt(chunkData, 8),
                    xOffset = readInt(chunkData, 12),
                    yOffset = readInt(chunkData, 16),
                    delayNum = readShort(chunkData, 20),
                    delayDen = readShort(chunkData, 22),
                    disposeOp = chunkData[24].toInt(),
                    blendOp = chunkData[25].toInt()
                ))
            }
            "IDAT", "fdAT" -> {
                commonChunks.add(chunkData)
            }
        }
    }
    
    return ApngParseResult(
        hasActl = hasActl,
        ihdrData = ihdrData,
        actlData = actlData,
        frameInfoList = frameInfoList,
        commonChunks = commonChunks
    )
}

/**
 * 构建每一帧的完整图像数据
 */
private fun buildFrames(
    ihdr: IhdrData,
    commonChunks: List<ByteArray>,
    frameInfoList: List<FrameInfo>,
    fullData: ByteArray
): List<ApngFrame> {
    // 简化实现：直接使用整个图像数据
    // 完整实现需要根据每帧的偏移量和大小提取单独的帧数据
    
    return frameInfoList.mapIndexed { index, frameInfo ->
        val image = Image.makeFromEncoded(fullData) ?: return emptyList()
        val bitmap = image.toComposeImageBitmap()
        image.close()
        
        val delayMs = if (frameInfo.delayDen.toInt() == 0) {
            100
        } else {
            (frameInfo.delayNum.toFloat() / frameInfo.delayDen.toFloat() * 1000).toLong()
        }
        
        ApngFrame(
            bitmap = bitmap,
            delayMs = delayMs,
            width = bitmap.width,
            height = bitmap.height
        )
    }
}

private fun readInt(data: ByteArray, offset: Int): Int {
    return (
        (data[offset].toInt() and 0xFF shl 24) or
        (data[offset + 1].toInt() and 0xFF shl 16) or
        (data[offset + 2].toInt() and 0xFF shl 8) or
        (data[offset + 3].toInt() and 0xFF)
    )
}

private fun readShort(data: ByteArray, offset: Int): Short {
    return (
        ((data[offset].toInt() and 0xFF) shl 8) or
        (data[offset + 1].toInt() and 0xFF)
    ).toShort()
}

private data class ApngParseResult(
    val hasActl: Boolean,
    val ihdrData: IhdrData?,
    val actlData: ActlData?,
    val frameInfoList: List<FrameInfo>,
    val commonChunks: List<ByteArray>
)

private data class IhdrData(
    val width: Int,
    val height: Int
)

private data class ActlData(
    val numFrames: Int,
    val numPlays: Int
)

private data class FrameInfo(
    val sequenceNumber: Int,
    val width: Int,
    val height: Int,
    val xOffset: Int,
    val yOffset: Int,
    val delayNum: Short,
    val delayDen: Short,
    val disposeOp: Int,
    val blendOp: Int
)
