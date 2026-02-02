package io.github.lugf027.apng.network

/**
 * Web (WASM/JS) implementation of ApngResourceLoader.
 * On web, resources must be loaded via network (URL source) or provided as bytes.
 * File and Resource sources are not supported in web environment.
 */
internal class WebApngResourceLoader : ApngResourceLoader {
    override suspend fun load(source: ApngSource): ByteArray {
        return when (source) {
            is ApngSource.Bytes -> source.data
            is ApngSource.Url -> {
                throw IllegalArgumentException(
                    "URL source not supported in resource loader. Use ApngLoader.loadFromUrl instead."
                )
            }
            is ApngSource.File -> {
                throw IllegalArgumentException(
                    "File access is not available in web environment. " +
                    "Use Url or Bytes sources instead."
                )
            }
            is ApngSource.Resource -> {
                throw IllegalArgumentException(
                    "Resource loading is not available in web environment. " +
                    "Use Url source to load from server, or provide bytes directly."
                )
            }
        }
    }
}

/**
 * Create the web resource loader.
 */
actual suspend fun createResourceLoader(): ApngResourceLoader {
    return WebApngResourceLoader()
}
