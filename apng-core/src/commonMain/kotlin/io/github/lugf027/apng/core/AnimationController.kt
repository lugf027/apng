package io.github.lugf027.apng.core

import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger

/**
 * APNG 动画控制器
 * 管理帧索引、播放状态、延迟等
 */
class AnimationController(
    private val apngImage: ApngImage
) {
    private var currentFrameIndex = 0
    private var isPlaying = false
    private var playbackSpeed = 1.0f
    private var loopCount = 0
    private var currentLoop = 0

    init {
        ApngLogger.d(ApngLogTags.ANIMATION) { "AnimationController created: ${apngImage.numFrames} frames, animated=${apngImage.isAnimated}" }
    }

    val frameCount: Int
        get() = apngImage.numFrames

    val currentFrame: ApngFrame?
        get() = if (currentFrameIndex < apngImage.frames.size) {
            apngImage.frames[currentFrameIndex]
        } else {
            null
        }

    val isAnimated: Boolean
        get() = apngImage.isAnimated

    val playing: Boolean
        get() = isPlaying

    /**
     * 开始播放动画
     */
    fun play() {
        ApngLogger.d(ApngLogTags.ANIMATION, "Animation play started")
        isPlaying = true
        currentLoop = 0
    }

    /**
     * 暂停播放动画
     */
    fun pause() {
        ApngLogger.d(ApngLogTags.ANIMATION, "Animation paused at frame $currentFrameIndex")
        isPlaying = false
    }

    /**
     * 停止播放并重置
     */
    fun stop() {
        ApngLogger.d(ApngLogTags.ANIMATION, "Animation stopped and reset")
        isPlaying = false
        currentFrameIndex = 0
        currentLoop = 0
    }

    /**
     * 前进到下一帧
     */
    fun nextFrame() {
        if (!isAnimated) return

        currentFrameIndex++
        if (currentFrameIndex >= frameCount) {
            currentFrameIndex = 0
            currentLoop++
            ApngLogger.v(ApngLogTags.ANIMATION) { "Animation loop completed, currentLoop=$currentLoop" }
        }
        ApngLogger.v(ApngLogTags.ANIMATION) { "Advanced to frame $currentFrameIndex" }
    }

    /**
     * 设置当前帧
     */
    fun setFrameIndex(index: Int) {
        if (index < 0 || index >= frameCount) {
            ApngLogger.w(ApngLogTags.ANIMATION, "Frame index out of range: $index (frameCount=$frameCount)")
            throw IllegalArgumentException("Frame index out of range: $index")
        }
        ApngLogger.v(ApngLogTags.ANIMATION) { "Set frame index to $index" }
        currentFrameIndex = index
    }

    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        if (speed <= 0) {
            ApngLogger.w(ApngLogTags.ANIMATION, "Invalid playback speed: $speed")
            throw IllegalArgumentException("Playback speed must be positive")
        }
        ApngLogger.d(ApngLogTags.ANIMATION) { "Playback speed set to $speed" }
        playbackSpeed = speed
    }

    /**
     * 获取播放速度
     */
    fun getPlaybackSpeed(): Float = playbackSpeed

    /**
     * 获取当前帧延迟（毫秒）
     */
    fun getCurrentFrameDelay(): Long {
        val frame = currentFrame ?: return 100
        return (frame.delayMillis / playbackSpeed).toLong()
    }

    /**
     * 获取当前循环次数
     */
    fun getCurrentLoop(): Int = currentLoop
}
