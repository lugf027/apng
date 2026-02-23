package io.github.lugf027.apng.internal.apng

internal data class PngChunk(
    val type: Int,
    val data: ByteArray,
    val crc: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PngChunk) return false
        return type == other.type && data.contentEquals(other.data) && crc == other.crc
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + data.contentHashCode()
        result = 31 * result + crc
        return result
    }
}

internal data class RawApngFrame(
    val pngBytes: ByteArray,
    val xOffset: Int,
    val yOffset: Int,
    val width: Int,
    val height: Int,
    val delayNum: Int,
    val delayDen: Int,
    val disposeOp: DisposeOp,
    val blendOp: BlendOp
) {
    val delayMs: Float
        get() {
            val den = if (delayDen == 0) 100 else delayDen
            return (delayNum.toFloat() / den.toFloat()) * 1000f
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawApngFrame) return false
        return pngBytes.contentEquals(other.pngBytes) &&
            xOffset == other.xOffset && yOffset == other.yOffset &&
            width == other.width && height == other.height &&
            delayNum == other.delayNum && delayDen == other.delayDen &&
            disposeOp == other.disposeOp && blendOp == other.blendOp
    }

    override fun hashCode(): Int {
        var result = pngBytes.contentHashCode()
        result = 31 * result + xOffset
        result = 31 * result + yOffset
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + delayNum
        result = 31 * result + delayDen
        result = 31 * result + disposeOp.hashCode()
        result = 31 * result + blendOp.hashCode()
        return result
    }
}

internal data class ApngAnimationData(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val numPlays: Int,
    val numFrames: Int,
    val frames: List<RawApngFrame>,
    val ihdrData: ByteArray,
    val auxiliaryChunks: List<PngChunk>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ApngAnimationData) return false
        return canvasWidth == other.canvasWidth && canvasHeight == other.canvasHeight &&
            numPlays == other.numPlays && numFrames == other.numFrames &&
            frames == other.frames && ihdrData.contentEquals(other.ihdrData) &&
            auxiliaryChunks == other.auxiliaryChunks
    }

    override fun hashCode(): Int {
        var result = canvasWidth
        result = 31 * result + canvasHeight
        result = 31 * result + numPlays
        result = 31 * result + numFrames
        result = 31 * result + frames.hashCode()
        result = 31 * result + ihdrData.contentHashCode()
        result = 31 * result + auxiliaryChunks.hashCode()
        return result
    }
}
