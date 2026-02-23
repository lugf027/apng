@file:JvmName("CoreUrlCompositionSpec")

package io.github.lugf027.apng

import kotlin.jvm.JvmName

@OptIn(InternalApngApi::class)
public fun ApngCompositionSpec.Companion.Url(
    url: String,
    request: suspend (url: String) -> ByteArray,
    cacheStrategy: ApngCacheStrategy = DiskCacheStrategy.Instance,
): ApngCompositionSpec = NetworkCompositionSpec(
    url = url,
    request = request,
    cacheStrategy = cacheStrategy,
)

private class NetworkCompositionSpec(
    private val url: String,
    private val request: suspend (url: String) -> ByteArray,
    private val cacheStrategy: ApngCacheStrategy,
) : ApngCompositionSpec {

    override val key: String
        get() = "url_$url"

    override suspend fun load(): ApngComposition {
        val bytes = networkLoad(request, cacheStrategy, url)
        checkNotNull(bytes) { "Failed to load APNG from $url" }
        return ApngComposition.parse(bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NetworkCompositionSpec) return false
        return url == other.url && request == other.request && cacheStrategy == other.cacheStrategy
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + cacheStrategy.hashCode()
        return result
    }
}
