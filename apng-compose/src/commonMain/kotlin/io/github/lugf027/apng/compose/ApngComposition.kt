package io.github.lugf027.apng.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
 * APNG composition loading result.
 *
 * A [ApngCompositionResult] subclass is returned from [rememberApngComposition].
 * It can be completed with a result or exception only one time.
 *
 * This class implements [State]<[ApngComposition]?> so you can use it like:
 * ```kotlin
 * val compositionResult = rememberApngComposition { ... }
 * // Or with delegate
 * val composition by rememberApngComposition { ... }
 * ```
 *
 * Use the former if you need to explicitly differentiate between loading and error states
 * or if you need to call [await] in a coroutine.
 *
 * Inspired by compottie's LottieCompositionResult.
 *
 * @see rememberApngComposition
 */
@Stable
public interface ApngCompositionResult : State<ApngComposition?> {

    /**
     * The composition or null if it hasn't yet loaded or failed to load.
     */
    override val value: ApngComposition?

    /**
     * The exception that was thrown while trying to load and parse the composition.
     */
    public val error: Throwable?

    /**
     * Whether the composition is still being loaded and parsed.
     */
    public val isLoading: Boolean

    /**
     * Whether the composition has completed (success or failure).
     */
    public val isComplete: Boolean

    /**
     * Whether the composition failed to load.
     */
    public val isFailure: Boolean

    /**
     * Whether the composition has loaded successfully.
     */
    public val isSuccess: Boolean

    /**
     * Suspend until the composition has finished parsing.
     *
     * @throws Throwable if the composition fails to load
     */
    public suspend fun await(): ApngComposition

    /**
     * Suspend until the composition has finished parsing.
     *
     * @return The composition or null if loading failed
     */
    public suspend fun awaitOrNull(): ApngComposition? {
        return try {
            await()
        } catch (_: Throwable) {
            null
        }
    }

    // Legacy sealed class compatibility
    public companion object {
        /** Create a Loading result */
        public val Loading: ApngCompositionResult = ApngCompositionResultImpl()

        /** Create a Success result */
        public fun Success(composition: ApngComposition): ApngCompositionResult =
            ApngCompositionResultImpl().also { it.completeSync(composition) }

        /** Create an Error result */
        public fun Error(throwable: Throwable): ApngCompositionResult =
            ApngCompositionResultImpl().also { it.completeExceptionallySync(throwable) }
    }
}

/**
 * Internal implementation of [ApngCompositionResult].
 */
@Stable
internal class ApngCompositionResultImpl : ApngCompositionResult {

    private var compositionDeferred = CompletableDeferred<ApngComposition>()
    private val mutex = Mutex()

    override var value: ApngComposition? by mutableStateOf(null)
        private set

    override var error: Throwable? by mutableStateOf(null)
        private set

    override val isLoading: Boolean by derivedStateOf { value == null && error == null }

    override val isComplete: Boolean by derivedStateOf { value != null || error != null }

    override val isFailure: Boolean by derivedStateOf { error != null }

    override val isSuccess: Boolean by derivedStateOf { value != null }

    override suspend fun await(): ApngComposition {
        return compositionDeferred.await()
    }

    /**
     * Complete with a successful composition.
     * Must be called from Main thread for state updates.
     */
    internal suspend fun complete(composition: ApngComposition) {
        mutex.withLock {
            if (isComplete) return@withLock
            value = composition
            compositionDeferred.complete(composition)
        }
    }

    /**
     * Complete with an error.
     * Must be called from Main thread for state updates.
     */
    internal suspend fun completeExceptionally(error: Throwable) {
        mutex.withLock {
            if (isComplete) return@withLock
            this.error = error
            compositionDeferred.completeExceptionally(error)
        }
    }

    /**
     * Synchronous completion for factory methods.
     */
    internal fun completeSync(composition: ApngComposition) {
        if (isComplete) return
        value = composition
        compositionDeferred.complete(composition)
    }

    /**
     * Synchronous error completion for factory methods.
     */
    internal fun completeExceptionallySync(error: Throwable) {
        if (isComplete) return
        this.error = error
        compositionDeferred.completeExceptionally(error)
    }
}
