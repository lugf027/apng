package io.github.lugf027.apng.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

/**
 * APNG composition spec for loading from various sources.
 * Provides a unified way to specify APNG sources.
 *
 * Available factory methods:
 * - [Bytes]: Load from a [ByteArray]
 * - `Resource`: Load from Compose Resources (requires `apng-resources` module)
 * - `ResourceBytes`: Load from Compose Resources with full path support (requires `apng-resources` module)
 * - `Url`: Load from network URL (requires `apng-network` module)
 *
 * Inspired by compottie's LottieCompositionSpec design.
 *
 * Example usage:
 * ```kotlin
 * // From byte array
 * val spec1 = ApngCompositionSpec.Bytes(byteArray)
 *
 * // From Compose Resources (requires apng-resources module)
 * val spec2 = ApngCompositionSpec.Resource(
 *     resourcePath = "animation.apng",
 *     readBytes = Res::readBytes
 * )
 *
 * // From Compose Resources with full path (requires apng-resources module)
 * val spec3 = ApngCompositionSpec.ResourceBytes(
 *     path = "files/animation.apng",
 *     directory = "",  // empty since path already includes directory
 *     readBytes = Res::readBytes
 * )
 *
 * // From network URL (requires apng-network module)
 * val spec4 = ApngCompositionSpec.Url("https://example.com/animation.apng")
 * ```
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

    /**
     * Companion object providing factory methods for creating [ApngCompositionSpec] instances.
     *
     * Core factory methods defined here:
     * - [Bytes]: Create spec from [ByteArray]
     *
     * Additional factory methods available via extension functions:
     * - `Resource()`: Load from Compose Resources (defined in `apng-resources` module)
     * - `ResourceBytes()`: Load from Compose Resources with full path support (defined in `apng-resources` module)
     * - `Url()`: Load from network URL (defined in `apng-network` module)
     *
     * @see Bytes for loading from byte array
     */
    public companion object {

        /**
         * Create an [ApngCompositionSpec] from a [ByteArray].
         *
         * Example:
         * ```kotlin
         * val composition by rememberApngComposition {
         *     ApngCompositionSpec.Bytes(apngByteArray)
         * }
         * ```
         *
         * @param data The APNG data as bytes
         * @return An [ApngCompositionSpec] that loads from the byte array
         */
        @Stable
        public fun Bytes(data: ByteArray): ApngCompositionSpec = BytesCompositionSpec(data)

        // Extension functions for additional loading methods:
        //
        // From apng-resources module:
        //   - Resource(resourcePath, readBytes, directory): Load from Compose Resources
        //   - ResourceBytes(path, directory, readBytes): Load from Compose Resources with full path
        //
        // From apng-network module:
        //   - Url(url, cacheStrategy): Load from network URL
    }
}

/**
 * Implementation of [ApngCompositionSpec] for loading APNG from byte array.
 *
 * @property data The APNG data as bytes
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
        return "BytesCompositionSpec(size=${data.size}, key='$key')"
    }
}
