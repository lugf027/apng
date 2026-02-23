package io.github.lugf027.apng

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
public sealed class ApngClipSpec {

    internal abstract fun getMinProgress(composition: ApngComposition): Float

    internal abstract fun getMaxProgress(composition: ApngComposition): Float

    @Immutable
    public class Frame(
        public val min: Int? = null,
        public val max: Int? = null,
        public val maxInclusive: Boolean = true,
    ) : ApngClipSpec() {

        private val actualMaxFrame = when {
            max == null -> null
            maxInclusive -> max
            else -> max - 1
        }

        override fun getMinProgress(composition: ApngComposition): Float {
            return when (min) {
                null -> 0f
                else -> {
                    val totalDuration = composition.cumulativeDurationsMs.lastOrNull() ?: return 0f
                    if (min <= 0) 0f
                    else (composition.cumulativeDurationsMs.getOrElse(min - 1) { 0f } / totalDuration).coerceIn(0f, 1f)
                }
            }
        }

        override fun getMaxProgress(composition: ApngComposition): Float {
            return when (actualMaxFrame) {
                null -> 1f
                else -> {
                    val totalDuration = composition.cumulativeDurationsMs.lastOrNull() ?: return 1f
                    (composition.cumulativeDurationsMs.getOrElse(actualMaxFrame) { totalDuration } / totalDuration).coerceIn(0f, 1f)
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Frame) return false
            return min == other.min && max == other.max && maxInclusive == other.maxInclusive
        }

        override fun hashCode(): Int {
            var result = min ?: 0
            result = 31 * result + (max ?: 0)
            result = 31 * result + maxInclusive.hashCode()
            return result
        }
    }

    @Immutable
    public class Progress(
        public val min: Float = 0f,
        public val max: Float = 1f,
    ) : ApngClipSpec() {
        override fun getMinProgress(composition: ApngComposition): Float = min
        override fun getMaxProgress(composition: ApngComposition): Float = max

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Progress) return false
            return min == other.min && max == other.max
        }

        override fun hashCode(): Int {
            var result = min.hashCode()
            result = 31 * result + max.hashCode()
            return result
        }
    }
}
