package io.github.lugf027.apng

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import io.github.lugf027.apng.internal.apng.ApngFrameComposer
import io.github.lugf027.apng.internal.apng.ApngParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Stable
public class ApngComposition internal constructor(
    internal val frames: List<ImageBitmap>,
    internal val frameDurationsMs: List<Float>,
    internal val cumulativeDurationsMs: FloatArray,
) {
    public val width: Int get() = if (frames.isNotEmpty()) frames[0].width else 0

    public val height: Int get() = if (frames.isNotEmpty()) frames[0].height else 0

    public val duration: Duration
        get() = if (cumulativeDurationsMs.isNotEmpty()) {
            cumulativeDurationsMs.last().toDouble().milliseconds
        } else Duration.ZERO

    public val frameCount: Int get() = frames.size

    public var iterations: Int = 1
        @InternalApngApi set

    public var speed: Float = 1f
        @InternalApngApi set

    internal fun frameIndexAt(progress: Float): Int {
        if (frames.isEmpty()) return 0
        if (progress <= 0f) return 0
        if (progress >= 1f) return frames.lastIndex

        val totalDuration = cumulativeDurationsMs.lastOrNull() ?: return 0
        val targetTime = progress * totalDuration

        var low = 0
        var high = cumulativeDurationsMs.size - 1

        while (low < high) {
            val mid = (low + high) / 2
            if (cumulativeDurationsMs[mid] <= targetTime) {
                low = mid + 1
            } else {
                high = mid
            }
        }

        return low.coerceIn(0, frames.lastIndex)
    }

    public companion object {
        public fun parse(bytes: ByteArray): ApngComposition {
            val animationData = ApngParser.parse(bytes)
            val composedFrames = ApngFrameComposer.compose(animationData)

            val frameBitmaps = composedFrames.map { it.bitmap }
            val frameDurations = composedFrames.map { it.delayMs }

            val cumulativeDurations = FloatArray(frameDurations.size)
            var cumulative = 0f
            for (i in frameDurations.indices) {
                cumulative += frameDurations[i]
                cumulativeDurations[i] = cumulative
            }

            return ApngComposition(
                frames = frameBitmaps,
                frameDurationsMs = frameDurations,
                cumulativeDurationsMs = cumulativeDurations,
            ).also {
                @OptIn(InternalApngApi::class)
                if (animationData.numPlays == 0) {
                    it.iterations = Apng.IterateForever
                } else {
                    it.iterations = animationData.numPlays
                }
            }
        }
    }
}

@OptIn(InternalApngApi::class)
@Composable
public fun rememberApngComposition(
    vararg keys: Any?,
    cache: ApngCompositionCache? = LocalApngCache.current,
    coroutineContext: CoroutineContext = remember { Apng.ioDispatcher() },
    spec: suspend () -> ApngCompositionSpec,
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

@OptIn(InternalApngApi::class)
@Composable
public fun rememberApngComposition(
    spec: ApngCompositionSpec,
): ApngCompositionResult {

    val result = remember(spec) {
        ApngCompositionResultImpl()
    }

    LaunchedEffect(result) {
        try {
            val composition = withContext(Apng.ioDispatcher()) { spec.load() }
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
