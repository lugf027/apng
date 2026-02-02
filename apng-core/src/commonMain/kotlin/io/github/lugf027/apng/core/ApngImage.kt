package io.github.lugf027.apng.core

/**
 * APNG 图像数据类
 */
data class ApngImage(
    val width: Int,
    val height: Int,
    val isAnimated: Boolean,
    val numFrames: Int = 1,
    val frames: List<ApngFrame> = emptyList(),
    val defaultImage: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ApngImage) return false

        if (width != other.width) return false
        if (height != other.height) return false
        if (isAnimated != other.isAnimated) return false
        if (numFrames != other.numFrames) return false
        if (frames != other.frames) return false
        if (defaultImage != null) {
            if (other.defaultImage == null) return false
            if (!defaultImage.contentEquals(other.defaultImage)) return false
        } else if (other.defaultImage != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + isAnimated.hashCode()
        result = 31 * result + numFrames
        result = 31 * result + frames.hashCode()
        result = 31 * result + (defaultImage?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * APNG 帧数据
 */
data class ApngFrame(
    val index: Int,
    val data: ByteArray,
    val delayNum: Int = 100,
    val delayDen: Int = 1000,
    val disposeOp: Int = 0,
    val blendOp: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val offsetX: Int = 0,
    val offsetY: Int = 0
) {
    val delayMillis: Long
        get() = (delayNum.toLong() * 1000) / delayDen

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ApngFrame) return false

        if (index != other.index) return false
        if (!data.contentEquals(other.data)) return false
        if (delayNum != other.delayNum) return false
        if (delayDen != other.delayDen) return false
        if (disposeOp != other.disposeOp) return false
        if (blendOp != other.blendOp) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (offsetX != other.offsetX) return false
        if (offsetY != other.offsetY) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + data.contentHashCode()
        result = 31 * result + delayNum
        result = 31 * result + delayDen
        result = 31 * result + disposeOp
        result = 31 * result + blendOp
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + offsetX
        result = 31 * result + offsetY
        return result
    }

    companion object {
        const val DISPOSE_OP_NONE = 0
        const val DISPOSE_OP_BACKGROUND = 1
        const val DISPOSE_OP_PREVIOUS = 2

        const val BLEND_OP_SOURCE = 0
        const val BLEND_OP_OVER = 1
    }
}
