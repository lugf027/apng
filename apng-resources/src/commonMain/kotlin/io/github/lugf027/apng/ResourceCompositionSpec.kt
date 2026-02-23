package io.github.lugf027.apng

import androidx.compose.runtime.Stable

/**
 * Create an [ApngCompositionSpec] that loads APNG data from Compose Resources.
 *
 * Usage:
 * ```
 * val composition by rememberApngComposition {
 *     ApngCompositionSpec.ComposeResource(readBytes = { Res.readBytes("files/animation.apng") })
 * }
 * ```
 *
 * @param cacheKey unique key for caching. Defaults to "compose_resource".
 * @param readBytes suspend function that reads the APNG file bytes from Compose Resources.
 * [`Res.readBytes`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html#raw-files)
 * should be used as the source.
 */
@Stable
public fun ApngCompositionSpec.Companion.ComposeResource(
    cacheKey: String = "compose_resource",
    readBytes: suspend () -> ByteArray,
): ApngCompositionSpec = ResourceCompositionSpecImpl(
    cacheKey = cacheKey,
    readBytes = readBytes,
)

private class ResourceCompositionSpecImpl(
    private val cacheKey: String,
    private val readBytes: suspend () -> ByteArray,
) : ApngCompositionSpec {

    override val key: String
        get() = "resource_$cacheKey"

    override suspend fun load(): ApngComposition {
        val bytes = readBytes()
        return ApngComposition.parse(bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceCompositionSpecImpl) return false
        return cacheKey == other.cacheKey && readBytes == other.readBytes
    }

    override fun hashCode(): Int {
        var result = cacheKey.hashCode()
        result = 31 * result + readBytes.hashCode()
        return result
    }
}
