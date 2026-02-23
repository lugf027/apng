package io.github.lugf027.apng

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

@Stable
public interface ApngCompositionSpec {

    public val key: String?

    public suspend fun load(): ApngComposition

    public companion object {

        @Stable
        public fun Bytes(
            bytes: ByteArray,
            cacheKey: String? = "bytes_${bytes.contentHashCode()}"
        ): ApngCompositionSpec = BytesImpl(bytes, cacheKey)
    }
}

@Immutable
private class BytesImpl(
    private val bytes: ByteArray,
    private val cacheKey: String?
) : ApngCompositionSpec {

    override val key: String?
        get() = cacheKey

    override suspend fun load(): ApngComposition {
        return ApngComposition.parse(bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BytesImpl) return false
        return bytes.contentEquals(other.bytes) && cacheKey == other.cacheKey
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + (cacheKey?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Bytes(cacheKey=$cacheKey)"
}
