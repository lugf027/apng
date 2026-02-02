package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    var frameIndex by remember { mutableIntStateOf(0) }
    
    // 根据 progress 计算帧索引
    LaunchedEffect(composition, progress) {
        if (composition == null || composition.frames.isEmpty()) return@LaunchedEffect
        
        val p = progress()
        frameIndex = (p * composition.frames.size).toInt().coerceIn(0, composition.frames.size - 1)
    }
    
    // frameIndex 变化时会触发重组，返回新的 Painter
    return remember(composition, frameIndex) {
        ApngPainter(composition, frameIndex)
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
    // 使用 by 委托来驱动帧变化和重组
    var frameIndex by remember { mutableIntStateOf(0) }
    
    // 自动播放动画
    LaunchedEffect(composition, autoPlay, speed, iterations) {
        if (composition == null || !autoPlay || composition.frames.isEmpty()) {
            return@LaunchedEffect
        }
        
        var loopCount = 0
        val maxLoops = if (iterations == 0) Int.MAX_VALUE else iterations
        
        while (isActive && loopCount < maxLoops) {
            for (index in composition.frames.indices) {
                if (!isActive) break
                
                // 更新帧索引 - 这会触发 Compose 重组
                frameIndex = index
                
                val frame = composition.frames[index]
                val actualDelay = (frame.delayMs / speed).toLong().coerceAtLeast(16L)
                delay(actualDelay)
            }
            loopCount++
        }
    }
    
    // frameIndex 变化时返回新的 Painter 实例来触发重绘
    return remember(composition, frameIndex) {
        ApngPainter(composition, frameIndex)
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
    private val currentFrameIndex: Int
) : Painter() {
    
    override val intrinsicSize: Size
        get() = composition?.let { 
            Size(it.width.toFloat(), it.height.toFloat()) 
        } ?: Size.Unspecified
    
    override fun DrawScope.onDraw() {
        val comp = composition ?: return
        if (comp.frames.isEmpty()) return
        
        val frameIndex = currentFrameIndex.coerceIn(0, comp.frames.size - 1)
        val frame = comp.frames[frameIndex]
        
        // 简化绘制 - 直接绘制到整个画布区域
        drawImage(
            image = frame.bitmap,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
}
