package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.lugf027.apng.core.AnimationController
import io.github.lugf027.apng.core.ApngImage
import kotlinx.coroutines.delay

/**
 * APNG 动画状态
 */
data class ApngAnimationState(
    val frameIndex: Int = 0,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val currentLoop: Int = 0
)

/**
 * APNG 动画回调接口
 */
interface ApngAnimationCallback {
    fun onFrameChanged(frameIndex: Int)
    fun onPlayStateChanged(isPlaying: Boolean)
    fun onLoopComplete(loopCount: Int)
}

/**
 * APNG 动画器
 * 管理 APNG 的播放逻辑
 */
@Composable
fun rememberApngAnimator(
    apngImage: ApngImage,
    autoPlay: Boolean = true,
    callback: ApngAnimationCallback? = null
): ApngAnimationState {
    val controller = remember { AnimationController(apngImage) }
    var animationState by remember { mutableStateOf(ApngAnimationState()) }

    LaunchedEffect(apngImage, autoPlay) {
        if (!apngImage.isAnimated) {
            animationState = ApngAnimationState(frameIndex = 0)
            return@LaunchedEffect
        }

        if (autoPlay) {
            controller.play()
            animationState = animationState.copy(isPlaying = true)

            while (controller.playing) {
                val delay = controller.getCurrentFrameDelay()
                delay(delay)

                val oldLoopCount = controller.getCurrentLoop()
                controller.nextFrame()
                val newLoopCount = controller.getCurrentLoop()

                animationState = animationState.copy(
                    frameIndex = controller.currentFrame?.index ?: 0,
                    currentLoop = newLoopCount
                )

                callback?.onFrameChanged(animationState.frameIndex)

                if (oldLoopCount != newLoopCount) {
                    callback?.onLoopComplete(newLoopCount)
                }
            }
        }
    }

    return animationState
}
