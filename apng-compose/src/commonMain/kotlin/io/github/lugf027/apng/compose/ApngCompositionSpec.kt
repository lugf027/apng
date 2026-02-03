package io.github.lugf027.apng.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

/**
 * APNG composition spec for loading from various sources.
 * Provides a unified way to specify APNG sources.
 * 
 * Inspired by compottie's LottieCompositionSpec design.
 */
@Stable
public interface ApngCompositionSpec {

    /**
     * Key that uniquely identifies composition instance.
     * Equal specs must return equal key for caching purposes.
     */
    public val key: String?

    /**
     * Load and parse the APNG data.
     * 
     * @return The parsed APNG composition
     * @throws Exception if loading or parsing fails
     */
    public suspend fun load(): ApngComposition

    public companion object {
        
        /**
         * Create an [ApngCompositionSpec] from a [ByteArray].
         * 
         * @param data The APNG data as bytes
         * @return An [ApngCompositionSpec] that loads from the byte array
         */
        @Stable
        public fun Bytes(data: ByteArray): ApngCompositionSpec = BytesCompositionSpec(data)
    }
}

/**
 * Implementation for loading APNG from byte array
 */
@Immutable
@JvmInline
internal value class BytesCompositionSpec(
    private val data: ByteArray
) : ApngCompositionSpec {

    override val key: String
        get() = "bytes_${data.contentHashCode()}"

    override suspend fun load(): ApngComposition {
        return ApngComposition.parse(data)
    }

    override fun toString(): String {
        return "Bytes(size=${data.size}, key=$key)"
    }
}
