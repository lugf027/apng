package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * APNG 帧数据
 */
@ConsistentCopyVisibility
data class ApngFrame internal constructor(
    internal val bitmap: ImageBitmap,
    val delayMs: Long,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val width: Int,
    val height: Int,
    val disposeOp: Int = 0,
    val blendOp: Int = 0
)

/**
 * APNG 合成数据 - 包含解析后的动画信息
 */
class ApngComposition internal constructor(
    val width: Int,
    val height: Int,
    internal val frames: List<ApngFrame>,
    val loopCount: Int,
    val isAnimated: Boolean
) {
    /**
     * 动画总时长（毫秒）
     */
    val durationMillis: Long = frames.sumOf { it.delayMs }
    
    /**
     * 帧数量
     */
    val frameCount: Int = frames.size
    
    companion object
}

/**
 * 加载 APNG 合成数据
 */
@Composable
fun rememberApngComposition(data: ByteArray): ApngCompositionResult {
    var result by remember { mutableStateOf<ApngCompositionResult>(ApngCompositionResult.Loading) }
    
    LaunchedEffect(data) {
        result = try {
            val composition = loadApngComposition(data)
            ApngCompositionResult.Success(composition)
        } catch (e: Throwable) {
            ApngCompositionResult.Error(e)
        }
    }
    
    return result
}

/**
 * APNG 合成加载结果
 */
sealed class ApngCompositionResult {
    data object Loading : ApngCompositionResult()
    data class Success(val composition: ApngComposition) : ApngCompositionResult()
    data class Error(val throwable: Throwable) : ApngCompositionResult()
    
    val value: ApngComposition?
        get() = (this as? Success)?.composition
}

/**
 * 加载 APNG 合成数据 - 平台特定实现
 */
internal expect fun loadApngComposition(data: ByteArray): ApngComposition

/**
 * 创建 APNG Painter
 * 
 * 参考 compottie 的 LottiePainter 设计，使用 Painter 抽象进行渲染
 * 
 * @param composition APNG 合成数据
 * @param progress 当前播放进度的 lambda（0.0-1.0）
 */
@Composable
fun rememberApngPainter(
    composition: ApngComposition?,
    progress: () -> Float = { 0f }
): Painter {
    return remember(composition) {
        ApngPainter(composition, progress)
    }
}

/**
 * 创建带自动播放的 APNG Painter
 * 
 * @param composition APNG 合成数据
 * @param autoPlay 是否自动播放
 * @param speed 播放速度倍率
 * @param iterations 循环次数，0 表示无限循环
 */
@Composable
fun rememberApngPainter(
    composition: ApngComposition?,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0
): Painter {
    var progress by remember { mutableFloatStateOf(0f) }
    var currentFrame by remember { mutableIntStateOf(0) }
    
    // 自动播放动画
    LaunchedEffect(composition, autoPlay, speed, iterations) {
        if (composition == null || !autoPlay || composition.frames.isEmpty()) {
            return@LaunchedEffect
        }
        
        var loopCount = 0
        val maxLoops = if (iterations == 0) Int.MAX_VALUE else iterations
        
        while (isActive && loopCount < maxLoops) {
            for (frameIndex in composition.frames.indices) {
                if (!isActive) break
                
                currentFrame = frameIndex
                progress = frameIndex.toFloat() / composition.frames.size.coerceAtLeast(1)
                
                val frame = composition.frames[frameIndex]
                val actualDelay = (frame.delayMs / speed).toLong().coerceAtLeast(16L)
                delay(actualDelay)
            }
            loopCount++
        }
    }
    
    return remember(composition) {
        ApngPainter(composition) { progress }
    }.also { painter ->
        // 更新 Painter 的状态触发重绘
        painter.updateFrame(currentFrame)
    }
}

/**
 * APNG Painter 实现
 * 
 * 继承自 Compose Painter，使用 DrawScope 进行绘制
 * 类似于 compottie 的 LottiePainter 设计
 */
internal class ApngPainter(
    private val composition: ApngComposition?,
    private val progress: () -> Float
) : Painter() {
    
    private var currentFrameIndex = 0
    
    override val intrinsicSize: Size
        get() = composition?.let { 
            Size(it.width.toFloat(), it.height.toFloat()) 
        } ?: Size.Unspecified
    
    /**
     * 更新当前帧索引
     */
    fun updateFrame(frameIndex: Int) {
        currentFrameIndex = frameIndex
    }
    
    override fun DrawScope.onDraw() {
        val comp = composition ?: return
        if (comp.frames.isEmpty()) return
        
        // 根据进度计算当前帧
        val frameIndex = if (comp.frames.size == 1) {
            0
        } else {
            currentFrameIndex.coerceIn(0, comp.frames.size - 1)
        }
        
        val frame = comp.frames[frameIndex]
        
        // 计算缩放以适应画布
        val scaleX = size.width / comp.width
        val scaleY = size.height / comp.height
        val scale = minOf(scaleX, scaleY)
        
        val scaledWidth = comp.width * scale
        val scaledHeight = comp.height * scale
        
        // 居中偏移
        val offsetX = (size.width - scaledWidth) / 2f
        val offsetY = (size.height - scaledHeight) / 2f
        
        // 绘制当前帧
        drawImage(
            image = frame.bitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(frame.bitmap.width, frame.bitmap.height),
            dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
            dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
        )
    }
}
