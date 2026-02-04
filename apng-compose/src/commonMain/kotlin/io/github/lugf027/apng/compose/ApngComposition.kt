package io.github.lugf027.apng.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap
import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger

/**
 * APNG frame data for compose rendering
 */
@ConsistentCopyVisibility
data class ApngFrame internal constructor(
    internal val bitmap: ImageBitmap,
    val delayMs: Long,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val width: Int,
    val height: Int,
    val disposeOp: Int = 0,
    val blendOp: Int = 0
)

/**
 * APNG composition data - contains parsed animation information.
 * 
 * This class is designed similar to compottie's LottieComposition.
 */
@Stable
class ApngComposition internal constructor(
    val width: Int,
    val height: Int,
    internal val frames: List<ApngFrame>,
    val loopCount: Int,
    val isAnimated: Boolean
) {
    /**
     * Total animation duration in milliseconds
     */
    val durationMillis: Long = frames.sumOf { it.delayMs }
    
    /**
     * Number of frames in the animation
     */
    val frameCount: Int = frames.size
    
    init {
        ApngLogger.d(ApngLogTags.COMPOSE) { "ApngComposition created: ${width}x${height}, $frameCount frames, duration=${durationMillis}ms, animated=$isAnimated" }
    }
    
    companion object {
        /**
         * Parse APNG data from byte array.
         * This is a platform-specific implementation.
         * 
         * @param data The APNG data as bytes
         * @return The parsed [ApngComposition]
         * @throws Exception if parsing fails
         */
        fun parse(data: ByteArray): ApngComposition {
            ApngLogger.d(ApngLogTags.COMPOSE, "Parsing APNG composition from ${data.size} bytes")
            return try {
                val composition = parseApngCompositionData(data)
                ApngLogger.i(ApngLogTags.COMPOSE) { "Successfully parsed ApngComposition: ${composition.width}x${composition.height}, ${composition.frameCount} frames" }
                composition
            } catch (e: Exception) {
                ApngLogger.e(ApngLogTags.COMPOSE, "Failed to parse APNG composition", e)
                throw e
            }
        }
    }
}

/**
 * Platform-specific APNG parsing implementation.
 * Each platform implements this to convert raw APNG bytes to ApngComposition.
 */
internal expect fun parseApngCompositionData(data: ByteArray): ApngComposition

/**
 * APNG composition loading result
 */
sealed class ApngCompositionResult {
    data object Loading : ApngCompositionResult()
    data class Success(val composition: ApngComposition) : ApngCompositionResult()
    data class Error(val throwable: Throwable) : ApngCompositionResult()
    
    val value: ApngComposition?
        get() = (this as? Success)?.composition
}
