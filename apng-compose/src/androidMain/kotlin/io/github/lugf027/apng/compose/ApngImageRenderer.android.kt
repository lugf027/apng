package io.github.lugf027.apng.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.lugf027.apng.core.ApngImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Android 平台的 APNG 渲染器实现
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
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var frameBitmaps by remember { mutableStateOf<List<FrameBitmapData>>(emptyList()) }

    // 初始化：解析图像
    LaunchedEffect(rawData) {
        try {
            // 尝试解析 APNG 帧
            val frames = parseApngFramesAndroid(rawData, apngImage)
            if (frames.isNotEmpty()) {
                frameBitmaps = frames
            } else {
                // 作为普通 PNG 加载
                val bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.size)
                if (bmp != null) {
                    bitmap = bmp
                }
            }
        } catch (e: Exception) {
            // 作为普通 PNG 加载
            try {
                val bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.size)
                if (bmp != null) {
                    bitmap = bmp
                }
            } catch (_: Exception) {
                // 加载失败
            }
        }
    }

    // 动画播放循环
    LaunchedEffect(autoPlay, frameBitmaps) {
        if (autoPlay && frameBitmaps.size > 1) {
            while (isActive) {
                val frame = frameBitmaps.getOrNull(currentFrameIndex)
                val delayMs = frame?.delayMs ?: 100L
                delay(delayMs.coerceAtLeast(16L))
                currentFrameIndex = (currentFrameIndex + 1) % frameBitmaps.size
            }
        }
    }

    // 释放资源
    DisposableEffect(Unit) {
        onDispose {
            bitmap?.recycle()
            frameBitmaps.forEach { it.bitmap.recycle() }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val imageToDraw = if (frameBitmaps.isNotEmpty()) {
            frameBitmaps.getOrNull(currentFrameIndex)?.bitmap
        } else {
            bitmap
        }

        imageToDraw?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = contentScale
            )
        }
    }
}

/**
 * 帧 Bitmap 数据
 */
private data class FrameBitmapData(
    val bitmap: Bitmap,
    val delayMs: Long
)

/**
 * Android 平台解析 APNG 帧
 */
private fun parseApngFramesAndroid(data: ByteArray, apngImage: ApngImage): List<FrameBitmapData> {
    val frames = mutableListOf<FrameBitmapData>()

    try {
        // 使用与 Desktop 类似的解析逻辑
        val parsedFrames = extractApngFramesAndroid(data)
        frames.addAll(parsedFrames)
    } catch (_: Exception) {
        // 解析失败
    }

    return frames
}

/**
 * 直接从 APNG 数据提取帧 (Android)
 */
private fun extractApngFramesAndroid(data: ByteArray): List<FrameBitmapData> {
    val frames = mutableListOf<FrameBitmapData>()

    // 收集公共 chunks
    val commonChunks = mutableListOf<ByteArray>()
    var ihdrChunk: ByteArray? = null

    data class FrameInfo(
        val width: Int,
        val height: Int,
        val delayNum: Int,
        val delayDen: Int,
        val idatData: MutableList<ByteArray> = mutableListOf()
    )

    val frameInfoList = mutableListOf<FrameInfo>()
    var currentFrame: FrameInfo? = null
    var isFirstFrame = true
    var globalWidth = 0
    var globalHeight = 0

    var offset = 8
    while (offset + 8 <= data.size) {
        val length = bytesToInt(data, offset)
        if (offset + 8 + length + 4 > data.size) break

        val typeBytes = data.sliceArray(offset + 4 until offset + 8)
        val type = typeBytes.decodeToString()
        val chunkData = data.sliceArray(offset + 8 until offset + 8 + length)
        val fullChunk = data.sliceArray(offset until offset + 8 + length + 4)

        when (type) {
            "IHDR" -> {
                ihdrChunk = fullChunk
                globalWidth = bytesToInt(chunkData, 0)
                globalHeight = bytesToInt(chunkData, 4)
            }
            "PLTE", "tRNS", "cHRM", "gAMA", "iCCP", "sBIT", "sRGB", "pHYs", "bKGD" -> {
                commonChunks.add(fullChunk)
            }
            "fcTL" -> {
                currentFrame?.let { frame ->
                    if (frame.idatData.isNotEmpty()) {
                        frameInfoList.add(frame)
                    }
                }

                val width = bytesToInt(chunkData, 4)
                val height = bytesToInt(chunkData, 8)
                val delayNum = bytesToShort(chunkData, 20)
                val delayDen = bytesToShort(chunkData, 22).let { if (it == 0) 1000 else it }

                currentFrame = FrameInfo(
                    width = width,
                    height = height,
                    delayNum = delayNum,
                    delayDen = delayDen
                )
                isFirstFrame = false
            }
            "IDAT" -> {
                if (currentFrame != null) {
                    currentFrame.idatData.add(chunkData)
                } else if (isFirstFrame) {
                    currentFrame = FrameInfo(
                        width = globalWidth,
                        height = globalHeight,
                        delayNum = 100,
                        delayDen = 1000
                    )
                    currentFrame.idatData.add(chunkData)
                }
            }
            "fdAT" -> {
                if (chunkData.size > 4) {
                    val frameData = chunkData.sliceArray(4 until chunkData.size)
                    currentFrame?.idatData?.add(frameData)
                }
            }
            "IEND" -> {
                currentFrame?.let { frame ->
                    if (frame.idatData.isNotEmpty()) {
                        frameInfoList.add(frame)
                    }
                }
                break
            }
        }

        offset += 8 + length + 4
    }

    val pngSignature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

    for (frameInfo in frameInfoList) {
        try {
            val pngOutput = mutableListOf<Byte>()
            pngOutput.addAll(pngSignature.toList())

            if (ihdrChunk != null) {
                val newIhdr = createIhdrChunk(frameInfo.width, frameInfo.height, ihdrChunk)
                pngOutput.addAll(newIhdr.toList())
            }

            for (chunk in commonChunks) {
                pngOutput.addAll(chunk.toList())
            }

            val combinedIdatData = mutableListOf<Byte>()
            for (idatChunk in frameInfo.idatData) {
                combinedIdatData.addAll(idatChunk.toList())
            }
            val idatChunk = createIdatChunk(combinedIdatData.toByteArray())
            pngOutput.addAll(idatChunk.toList())

            val iendChunk = createIendChunk()
            pngOutput.addAll(iendChunk.toList())

            val pngBytes = pngOutput.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.size)
            if (bitmap != null) {
                val delayMs = (frameInfo.delayNum.toLong() * 1000) / frameInfo.delayDen
                frames.add(FrameBitmapData(bitmap, delayMs.coerceAtLeast(16L)))
            }
        } catch (_: Exception) {
            // 跳过
        }
    }

    return frames
}

private fun createIhdrChunk(width: Int, height: Int, originalIhdr: ByteArray): ByteArray {
    val ihdrData = ByteArray(13)
    intToBytes(width).copyInto(ihdrData, 0)
    intToBytes(height).copyInto(ihdrData, 4)
    originalIhdr.copyInto(ihdrData, 8, 16, 21)
    return createChunk("IHDR", ihdrData)
}

private fun createIdatChunk(data: ByteArray): ByteArray {
    return createChunk("IDAT", data)
}

private fun createIendChunk(): ByteArray {
    return createChunk("IEND", ByteArray(0))
}

private fun createChunk(type: String, data: ByteArray): ByteArray {
    val typeBytes = type.toByteArray()
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
