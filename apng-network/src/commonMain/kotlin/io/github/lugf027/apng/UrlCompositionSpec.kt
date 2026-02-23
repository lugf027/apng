package io.github.lugf027.apng

/**
 * [ApngComposition] from network [url].
 *
 * @param cacheStrategy caching strategy. Caching to system temp dir by default.
 */
@OptIn(InternalApngApi::class)
public fun ApngCompositionSpec.Companion.Url(
    url: String,
    cacheStrategy: ApngCacheStrategy = DiskCacheStrategy.Instance,
): ApngCompositionSpec = Url(
    url = url,
    request = DefaultHttpRequest,
    cacheStrategy = cacheStrategy,
)
