package io.github.lugf027.apng

import okio.Path

public interface ApngCacheStrategy {

    public fun path(url: String): Path?

    public suspend fun save(url: String, bytes: ByteArray): Path?

    public suspend fun load(url: String): ByteArray?

    public suspend fun clear()
}
