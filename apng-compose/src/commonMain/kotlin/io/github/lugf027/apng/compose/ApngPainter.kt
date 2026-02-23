package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Remember and load APNG composition from a spec.
 * 
 * This is the main entry point for loading APNG animations,
 * inspired by compottie's rememberLottieComposition.
 * 
 * @param spec Lambda that returns the composition spec
 * @return The loading result (Loading, Success, or Error)
 */
@Composable
fun rememberApngComposition(
    spec: () -> ApngCompositionSpec
): ApngCompositionResult {
    var result by remember { mutableStateOf<ApngCompositionResult>(ApngCompositionResult.Loading) }
    val specValue = remember { spec() }
    
    LaunchedEffect(specValue.key) {
        result = ApngCompositionResult.Loading
        result = try {
            val composition = specValue.load()
            ApngCompositionResult.Success(composition)
        } catch (e: Throwable) {
            ApngCompositionResult.Error(e)
        }
    }
    
    return result
}

/**
 * Remember and load APNG composition from byte array.
 * 
 * @param data The APNG data as bytes
 * @return The loading result (Loading, Success, or Error)
 */
@Composable
fun rememberApngComposition(data: ByteArray): ApngCompositionResult {
    return rememberApngComposition { ApngCompositionSpec.Bytes(data) }
}

/**
 * Create APNG Painter
 * 
 * Inspired by compottie's LottiePainter design, using Painter abstraction for rendering.
 * 
 * @param composition APNG composition data
 * @param progress Current playback progress lambda (0.0-1.0)
 */
@Composable
fun rememberApngPainter(
    composition: ApngComposition?,
    progress: () -> Float = { 0f }
): Painter {
    var frameIndex by remember { mutableIntStateOf(0) }
    
    // Calculate frame index based on progress
    LaunchedEffect(composition, progress) {
        if (composition == null || composition.frames.isEmpty()) return@LaunchedEffect
        
        val p = progress()
        frameIndex = (p * composition.frames.size).toInt().coerceIn(0, composition.frames.size - 1)
    }
    
    // Return new Painter when frameIndex changes, triggering recomposition
    return remember(composition, frameIndex) {
        ApngPainter(composition, frameIndex)
    }
}

/**
 * Create APNG Painter with auto-play support
 * 
 * @param composition APNG composition data
 * @param autoPlay Whether to auto-play the animation
 * @param speed Playback speed multiplier
 * @param iterations Number of loops, 0 means infinite
 */
@Composable
fun rememberApngPainter(
    composition: ApngComposition?,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0
): Painter {
    // Use by delegate to drive frame changes and recomposition
    var frameIndex by remember { mutableIntStateOf(0) }
    
    // Auto-play animation
    LaunchedEffect(composition, autoPlay, speed, iterations) {
        if (composition == null || !autoPlay || composition.frames.isEmpty()) {
            return@LaunchedEffect
        }
        
        var loopCount = 0
        val maxLoops = if (iterations == 0) Int.MAX_VALUE else iterations
        
        while (isActive && loopCount < maxLoops) {
            for (index in composition.frames.indices) {
                if (!isActive) break
                
                // Update frame index - triggers Compose recomposition
                frameIndex = index
                
                val frame = composition.frames[index]
                val actualDelay = (frame.delayMs / speed).toLong().coerceAtLeast(16L)
                delay(actualDelay)
            }
            loopCount++
        }
    }
    
    // Return new Painter instance when frameIndex changes to trigger redraw
    return remember(composition, frameIndex) {
        ApngPainter(composition, frameIndex)
    }
}

/**
 * APNG Painter implementation
 * 
 * Inherits from Compose Painter, using DrawScope for rendering.
 * Similar to compottie's LottiePainter design.
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
        
        // Simplified drawing - draw directly to entire canvas area
        drawImage(
            image = frame.bitmap,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
}
