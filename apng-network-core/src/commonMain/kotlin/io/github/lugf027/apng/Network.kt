package io.github.lugf027.apng

internal suspend fun networkLoad(
    request: suspend (url: String) -> ByteArray,
    cacheStrategy: ApngCacheStrategy,
    url: String
): ByteArray? {
    return try {
        try {
            cacheStrategy.load(url)?.let { return it }
        } catch (_: Throwable) {
        }

        val bytes = request(url)

        try {
            cacheStrategy.save(url, bytes)
        } catch (e: Throwable) {
            Apng.logger?.error("Failed to cache downloaded data", e)
        }
        bytes
    } catch (_: Throwable) {
        null
    }
}
