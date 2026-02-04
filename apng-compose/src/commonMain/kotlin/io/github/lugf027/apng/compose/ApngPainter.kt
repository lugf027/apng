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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Load and prepare [ApngComposition] for displaying.
 *
 * Remembers the composition produced by [spec] if all values of [keys] are equal (`==`) to the
 * values they had in the previous composition, otherwise produce and remember a new
 * [ApngCompositionResult] by calling [spec] again.
 *
 * Example usage:
 * ```kotlin
 * // Basic usage with Resource
 * val composition by rememberApngComposition {
 *     ApngCompositionSpec.Resource("animation.apng", Res::readBytes)
 * }
 *
 * // With custom cache
 * val composition by rememberApngComposition(
 *     cache = ApngCompositionCache(size = 20)
 * ) {
 *     ApngCompositionSpec.Resource("animation.apng", Res::readBytes)
 * }
 *
 * // With additional keys for recomposition
 * val composition by rememberApngComposition(url) {
 *     ApngCompositionSpec.Url(url)
 * }
 *
 * // Using result explicitly
 * val result = rememberApngComposition { spec }
 * when {
 *     result.isLoading -> LoadingIndicator()
 *     result.isFailure -> ErrorView(result.error)
 *     result.isSuccess -> ApngImage(result.value!!)
 * }
 * ```
 *
 * Inspired by compottie's rememberLottieComposition.
 *
 * @param keys Additional keys to trigger recomposition
 * @param cache Cache instance for storing parsed compositions.
 *              Default is [LocalApngCache] which caches 10 compositions.
 *              Pass null to disable caching.
 * @param coroutineContext Context for loading operations. Defaults to IO dispatcher.
 * @param spec Lambda that returns the [ApngCompositionSpec] to load
 * @return The loading result (Loading, Success, or Error)
 */
@Composable
public fun rememberApngComposition(
    vararg keys: Any?,
    cache: ApngCompositionCache? = LocalApngCache.current,
    coroutineContext: CoroutineContext = remember { Dispatchers.Default },
    spec: suspend () -> ApngCompositionSpec
): ApngCompositionResult {
    val result = remember(*keys, cache) {
        ApngCompositionResultImpl()
    }

    LaunchedEffect(result, cache) {
        try {
            val composition = withContext(coroutineContext) {
                val specInstance = spec()
                cache?.getOrPut(specInstance.key, specInstance::load) ?: specInstance.load()
            }
            result.complete(composition)
        } catch (c: CancellationException) {
            result.completeExceptionally(c)
            throw c
        } catch (t: Throwable) {
            result.completeExceptionally(
                ApngException("Composition failed to load", t)
            )
        }
    }

    return result
}

/**
 * Remember and load APNG composition from byte array.
 *
 * @param data The APNG data as bytes
 * @return The loading result
 */
@Composable
public fun rememberApngComposition(data: ByteArray): ApngCompositionResult {
    return rememberApngComposition { ApngCompositionSpec.Bytes(data) }
}

/**
 * Create APNG Painter with progress control.
 *
 * Inspired by compottie's LottiePainter design, using Painter abstraction for rendering.
 *
 * @param composition APNG composition data
 * @param progress Current playback progress lambda (0.0-1.0)
 */
@Composable
public fun rememberApngPainter(
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
 * Create APNG Painter with auto-play support.
 *
 * @param composition APNG composition data
 * @param autoPlay Whether to auto-play the animation
 * @param speed Playback speed multiplier
 * @param iterations Number of loops, 0 means infinite
 */
@Composable
public fun rememberApngPainter(
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
 * APNG Painter implementation.
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

/**
 * Exception thrown when APNG composition loading fails.
 */
public class ApngException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
