package io.github.lugf027.apng

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
public fun animateApngCompositionAsState(
    composition: ApngComposition?,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    clipSpec: ApngClipSpec? = null,
    speed: Float = composition?.speed ?: 1f,
    iterations: Int = composition?.iterations ?: 1,
    cancellationBehavior: ApngCancellationBehavior = ApngCancellationBehavior.Immediately,
    useCompositionFrameRate: Boolean = false,
): ApngAnimationState {

    require(iterations > 0) { "Iterations must be a positive number ($iterations)." }
    require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }

    val animatable = rememberApngAnimatable()
    var wasPlaying by remember { mutableStateOf(isPlaying) }

    LaunchedEffect(
        composition,
        isPlaying,
        clipSpec,
        speed,
        iterations,
    ) {
        if (isPlaying && !wasPlaying && restartOnPlay) {
            animatable.resetToBeginning()
        }
        wasPlaying = isPlaying
        if (!isPlaying) return@LaunchedEffect

        animatable.animate(
            composition,
            iterations = iterations,
            reverseOnRepeat = reverseOnRepeat,
            speed = speed,
            clipSpec = clipSpec,
            initialProgress = animatable.progress,
            continueFromPreviousAnimate = false,
            cancellationBehavior = cancellationBehavior,
            useCompositionFrameRate = useCompositionFrameRate,
        )
    }

    return animatable
}
