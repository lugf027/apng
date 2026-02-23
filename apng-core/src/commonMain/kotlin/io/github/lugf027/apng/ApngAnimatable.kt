package io.github.lugf027.apng

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.js.JsName

@Composable
public fun rememberApngAnimatable(): ApngAnimatable = remember { ApngAnimatableImpl() }

@JsName("createApngAnimatable")
public fun ApngAnimatable(): ApngAnimatable = ApngAnimatableImpl()

public suspend fun ApngAnimatable.resetToBeginning() {
    snapTo(
        progress = defaultProgress(composition, clipSpec, speed),
        iteration = 1,
    )
}

@Stable
public interface ApngAnimatable : ApngAnimationState {

    public suspend fun snapTo(
        composition: ApngComposition? = this.composition,
        progress: Float = this.progress,
        iteration: Int = this.iteration,
        resetLastFrameNanos: Boolean = progress != this.progress,
    )

    public suspend fun animate(
        composition: ApngComposition?,
        iteration: Int = this.iteration,
        iterations: Int = this.iterations,
        reverseOnRepeat: Boolean = this.reverseOnRepeat,
        speed: Float = this.speed,
        clipSpec: ApngClipSpec? = this.clipSpec,
        initialProgress: Float = defaultProgress(composition, clipSpec, speed),
        continueFromPreviousAnimate: Boolean = false,
        cancellationBehavior: ApngCancellationBehavior = ApngCancellationBehavior.Immediately,
        useCompositionFrameRate: Boolean = false,
    )
}

@Stable
private class ApngAnimatableImpl : ApngAnimatable {
    override var isPlaying: Boolean by mutableStateOf(false)
        private set

    override val value: Float
        get() = progress

    override var iteration: Int by mutableStateOf(1)
        private set

    override var iterations: Int by mutableStateOf(1)
        private set

    override var reverseOnRepeat: Boolean by mutableStateOf(false)
        private set

    override var clipSpec: ApngClipSpec? by mutableStateOf(null)
        private set

    override var speed: Float by mutableStateOf(1f)
        private set

    override var useCompositionFrameRate: Boolean by mutableStateOf(false)
        private set

    private val frameSpeed: Float by derivedStateOf {
        if (reverseOnRepeat && iteration % 2 == 0) -speed else speed
    }

    override var composition: ApngComposition? by mutableStateOf(null)
        private set

    private var progressRaw: Float by mutableStateOf(0f)

    override var progress: Float by mutableStateOf(0f)
        private set

    override var lastFrameNanos: Long by mutableStateOf(AnimationConstants.UnspecifiedTime)
        private set

    private val endProgress: Float by derivedStateOf {
        val c = composition
        when {
            c == null -> 0f
            speed < 0 -> clipSpec?.getMinProgress(c) ?: 0f
            else -> clipSpec?.getMaxProgress(c) ?: 1f
        }
    }

    override val isAtEnd: Boolean by derivedStateOf { iteration == iterations && progress == endProgress }

    private val mutex = MutatorMutex()

    override suspend fun snapTo(
        composition: ApngComposition?,
        progress: Float,
        iteration: Int,
        resetLastFrameNanos: Boolean,
    ) {
        mutex.mutate {
            this.composition = composition
            updateProgress(progress)
            this.iteration = iteration
            isPlaying = false
            if (resetLastFrameNanos) {
                lastFrameNanos = AnimationConstants.UnspecifiedTime
            }
        }
    }

    override suspend fun animate(
        composition: ApngComposition?,
        iteration: Int,
        iterations: Int,
        reverseOnRepeat: Boolean,
        speed: Float,
        clipSpec: ApngClipSpec?,
        initialProgress: Float,
        continueFromPreviousAnimate: Boolean,
        cancellationBehavior: ApngCancellationBehavior,
        useCompositionFrameRate: Boolean,
    ) {
        mutex.mutate {
            this.iteration = iteration
            this.iterations = iterations
            this.reverseOnRepeat = reverseOnRepeat
            this.speed = speed
            this.clipSpec = clipSpec
            this.composition = composition
            updateProgress(initialProgress)
            this.useCompositionFrameRate = useCompositionFrameRate
            if (!continueFromPreviousAnimate) lastFrameNanos = AnimationConstants.UnspecifiedTime
            if (composition == null) {
                isPlaying = false
                return@mutate
            } else if (speed.isInfinite()) {
                updateProgress(endProgress)
                isPlaying = false
                this.iteration = iterations
                return@mutate
            }

            isPlaying = true
            try {
                val context = when (cancellationBehavior) {
                    ApngCancellationBehavior.OnIterationFinish -> NonCancellable
                    ApngCancellationBehavior.Immediately -> EmptyCoroutineContext
                }
                val parentJob = coroutineContext.job
                withContext(context) {
                    while (true) {
                        val actualIterations = when (cancellationBehavior) {
                            ApngCancellationBehavior.OnIterationFinish -> {
                                if (parentJob.isActive) iterations else iteration
                            }
                            else -> iterations
                        }
                        if (!doFrame(actualIterations)) break
                    }
                }
                coroutineContext.ensureActive()
            } finally {
                isPlaying = false
            }
        }
    }

    private suspend fun doFrame(iterations: Int): Boolean {
        return if (iterations == Apng.IterateForever) {
            withInfiniteAnimationFrameNanos { frameNanos ->
                onFrame(iterations, frameNanos)
            }
        } else {
            withFrameNanos { frameNanos ->
                onFrame(iterations, frameNanos)
            }
        }
    }

    private fun onFrame(iterations: Int, frameNanos: Long): Boolean {
        val composition = composition ?: return true
        val dNanos = if (lastFrameNanos == AnimationConstants.UnspecifiedTime) 0L else (frameNanos - lastFrameNanos)
        lastFrameNanos = frameNanos

        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f

        val dProgress = dNanos / 1_000_000f / composition.duration.inWholeMilliseconds * frameSpeed
        val progressPastEndOfIteration = when {
            frameSpeed < 0 -> minProgress - (progressRaw + dProgress)
            else -> progressRaw + dProgress - maxProgress
        }
        if (progressPastEndOfIteration < 0f) {
            updateProgress(progressRaw.coerceIn(minProgress, maxProgress) + dProgress)
        } else {
            val durationProgress = maxProgress - minProgress
            if (durationProgress <= 0f) return false
            val dIterations = (progressPastEndOfIteration / durationProgress).toInt() + 1

            if (iteration + dIterations > iterations) {
                updateProgress(endProgress)
                iteration = iterations
                return false
            }
            iteration += dIterations
            val progressPastEndRem = progressPastEndOfIteration - (dIterations - 1) * durationProgress
            updateProgress(
                when {
                    frameSpeed < 0 -> maxProgress - progressPastEndRem
                    else -> minProgress + progressPastEndRem
                }
            )
        }

        return true
    }

    private fun updateProgress(progress: Float) {
        this.progressRaw = progress
        this.progress = progress
    }
}

private fun defaultProgress(composition: ApngComposition?, clipSpec: ApngClipSpec?, speed: Float): Float {
    return when {
        speed < 0 && composition == null -> 1f
        composition == null -> 0f
        speed < 0 -> clipSpec?.getMaxProgress(composition) ?: 1f
        else -> clipSpec?.getMinProgress(composition) ?: 0f
    }
}
