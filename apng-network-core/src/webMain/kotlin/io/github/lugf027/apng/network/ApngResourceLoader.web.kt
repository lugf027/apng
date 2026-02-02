package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

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

/**
 * Get the default cache directory for web platform.
 * Note: Web doesn't have filesystem access, this is a placeholder.
 */
internal actual fun getDefaultCacheDirectory(): Path {
    // Web platform doesn't support disk caching
    return "/tmp/apng-cache".toPath()
}

/**
 * Web platform uses FakeFileSystem for in-memory caching.
 * This provides a compatible API while storing data in memory.
 */
private val webFileSystem = FakeFileSystem()

/**
 * Get the file system for web platform.
 * Returns a FakeFileSystem for in-memory operations.
 */
internal actual fun getSystemFileSystem(): FileSystem = webFileSystem
