package io.github.lugf027.apng.core

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
        isPlaying = true
        currentLoop = 0
    }

    /**
     * 暂停播放动画
     */
    fun pause() {
        isPlaying = false
    }

    /**
     * 停止播放并重置
     */
    fun stop() {
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
        }
    }

    /**
     * 设置当前帧
     */
    fun setFrameIndex(index: Int) {
        if (index < 0 || index >= frameCount) {
            throw IllegalArgumentException("Frame index out of range: $index")
        }
        currentFrameIndex = index
    }

    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        if (speed <= 0) {
            throw IllegalArgumentException("Playback speed must be positive")
        }
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
