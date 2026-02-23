package io.github.lugf027.apng

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

public val LocalApngCache: ProvidableCompositionLocal<ApngCompositionCache> = compositionLocalOf {
    ApngCompositionCache(10)
}

public interface ApngCompositionCache {

    public suspend fun getOrPut(
        key: Any?,
        create: suspend () -> ApngComposition
    ): ApngComposition

    public suspend fun clear()

    public object Empty : ApngCompositionCache {
        override suspend fun getOrPut(
            key: Any?,
            create: suspend () -> ApngComposition
        ): ApngComposition = create()

        override suspend fun clear() {}
    }
}

public suspend fun ApngCompositionCache.prepare(spec: ApngCompositionSpec): ApngComposition {
    return getOrPut(spec.key, spec::load)
}

public fun ApngCompositionCache(size: Int): ApngCompositionCache = object : ApngCompositionCache {

    private val cache = LruMap<ApngComposition>(limit = size)

    override suspend fun getOrPut(
        key: Any?,
        create: suspend () -> ApngComposition
    ): ApngComposition {
        if (key == null) return create()
        return cache.getOrPutSuspend(key, create)
    }

    override suspend fun clear() {
        cache.clear()
    }
}
