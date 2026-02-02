package io.github.lugf027.apng.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import io.github.lugf027.apng.core.ApngImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect

/**
 * Desktop 平台的 APNG 渲染器实现
 * Skia 不原生支持 APNG 动画，需要自己解析帧数据
 */
@Composable
actual fun ApngImageRenderer(
    apngImage: ApngImage,
    rawData: ByteArray,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    autoPlay: Boolean
) {
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var frameImages by remember { mutableStateOf<List<FrameImageData>>(emptyList()) }
    var staticImage by remember { mutableStateOf<Image?>(null) }

    // 解析 APNG 帧
    LaunchedEffect(rawData) {
        try {
            val frames = parseApngFrames(rawData)
            if (frames.isNotEmpty()) {
                frameImages = frames
            } else {
                // 作为静态 PNG 加载
                staticImage = Image.makeFromEncoded(rawData)
            }
        } catch (e: Exception) {
            // 加载失败，尝试作为静态图像
            try {
                staticImage = Image.makeFromEncoded(rawData)
            } catch (_: Exception) {}
        }
    }

    // 动画播放循环
    LaunchedEffect(autoPlay, frameImages) {
        if (autoPlay && frameImages.size > 1) {
            while (isActive) {
                val frame = frameImages.getOrNull(currentFrameIndex)
                val delayMs = frame?.delayMs ?: 100L
                delay(delayMs.coerceAtLeast(16L))
                currentFrameIndex = (currentFrameIndex + 1) % frameImages.size
            }
        }
    }

    // 释放资源
    DisposableEffect(Unit) {
        onDispose {
            staticImage?.close()
            frameImages.forEach { it.image.close() }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val imageToDraw = if (frameImages.isNotEmpty()) {
                frameImages.getOrNull(currentFrameIndex)?.image
            } else {
                staticImage
            }

            imageToDraw?.let { image ->
                drawIntoCanvas { canvas ->
                    val imageWidth = image.width.toFloat()
                    val imageHeight = image.height.toFloat()

                    val scaleX = canvasWidth / imageWidth
                    val scaleY = canvasHeight / imageHeight
                    val scale = minOf(scaleX, scaleY)

                    val scaledWidth = imageWidth * scale
                    val scaledHeight = imageHeight * scale

                    val left = (canvasWidth - scaledWidth) / 2f
                    val top = (canvasHeight - scaledHeight) / 2f

                    canvas.nativeCanvas.drawImageRect(
                        image,
                        Rect.makeWH(imageWidth, imageHeight),
                        Rect.makeXYWH(left, top, scaledWidth, scaledHeight)
                    )
                }
            }
        }
    }
}

/**
 * 帧图像数据
 */
private data class FrameImageData(
    val image: Image,
    val delayMs: Long
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
 * 解析 APNG 帧 - 完整实现
 */
private fun parseApngFrames(data: ByteArray): List<FrameImageData> {
    val frames = mutableListOf<FrameImageData>()

    // 检查 PNG 签名
    if (data.size < 8 || !isPngSignature(data)) {
        return frames
    }

    // 收集公共 chunks
    val commonChunks = mutableListOf<ByteArray>()
    var ihdrData: ByteArray? = null
    var globalWidth = 0
    var globalHeight = 0

    val frameInfoList = mutableListOf<FrameInfo>()
    var currentFrameInfo: FrameInfo? = null
    var hasActl = false

    // 解析所有 chunks
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
            }
            "PLTE", "tRNS", "cHRM", "gAMA", "iCCP", "sBIT", "sRGB", "pHYs", "bKGD" -> {
                // 收集可能需要的辅助 chunks（完整 chunk 包括 length + type + data + crc）
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
                // IDAT 数据属于当前帧
                currentFrameInfo?.idatData?.add(chunkData)
                    ?: run {
                        // 如果没有 fcTL，创建默认帧
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
                        currentFrameInfo!!.idatData.add(chunkData)
                    }
            }
            "fdAT" -> {
                // fdAT: 4 字节序列号 + 帧数据
                if (chunkData.size > 4) {
                    val frameData = chunkData.sliceArray(4 until chunkData.size)
                    currentFrameInfo?.idatData?.add(frameData)
                }
            }
            "IEND" -> {
                // 保存最后一帧
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

    // 如果不是 APNG（没有 acTL），返回空列表让调用者作为静态图处理
    if (!hasActl || frameInfoList.isEmpty()) {
        return frames
    }

    // 构建每帧的 PNG 图像
    val canvasBitmap = Bitmap()
    canvasBitmap.allocPixels(ImageInfo.makeN32(globalWidth, globalHeight, ColorAlphaType.PREMUL))
    val canvas = Canvas(canvasBitmap)
    canvas.clear(0x00000000) // 透明背景

    var previousBitmap: Bitmap? = null

    for (frameInfo in frameInfoList) {
        try {
            // 构建这一帧的 PNG 数据
            val framePng = buildFramePng(ihdrData!!, commonChunks, frameInfo)
            val frameImage = Image.makeFromEncoded(framePng)

            if (frameImage != null) {
                // 根据 dispose_op 处理
                when (frameInfo.disposeOp) {
                    1 -> { // APNG_DISPOSE_OP_BACKGROUND - 绘制前保存，绘制后清除区域
                        previousBitmap = canvasBitmap.copy()
                    }
                    2 -> { // APNG_DISPOSE_OP_PREVIOUS - 绘制前保存
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
                val resultImage = Image.makeFromBitmap(canvasBitmap.copy())
                frames.add(FrameImageData(resultImage, frameInfo.delayMs.coerceAtLeast(16L)))

                frameImage.close()

                // 处理 dispose_op
                when (frameInfo.disposeOp) {
                    1 -> { // APNG_DISPOSE_OP_BACKGROUND - 清除绘制区域
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
                    2 -> { // APNG_DISPOSE_OP_PREVIOUS - 恢复到之前的状态
                        previousBitmap?.let { prev ->
                            canvas.clear(0x00000000)
                            canvas.drawImage(Image.makeFromBitmap(prev), 0f, 0f)
                            prev.close()
                        }
                        previousBitmap = null
                    }
                }
            }
        } catch (e: Exception) {
            // 跳过无法创建的帧
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

    // 创建新的 IHDR（使用帧的宽高）
    val newIhdrData = ByteArray(13)
    intToBytes(frameInfo.width).copyInto(newIhdrData, 0)
    intToBytes(frameInfo.height).copyInto(newIhdrData, 4)
    // 复制原始的 bit depth, color type 等
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

private fun isPngSignature(data: ByteArray): Boolean {
    val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    for (i in signature.indices) {
        if (data[i] != signature[i]) return false
    }
    return true
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
