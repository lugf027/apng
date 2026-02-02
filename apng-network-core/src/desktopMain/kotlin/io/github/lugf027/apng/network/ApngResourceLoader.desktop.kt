package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Desktop (JVM) implementation of ApngResourceLoader.
 * Loads resources from file system using Okio.
 */
internal class DesktopApngResourceLoader : ApngResourceLoader {
    private val fileSystem = FileSystem.SYSTEM

    override suspend fun load(source: ApngSource): ByteArray {
        return when (source) {
            is ApngSource.Bytes -> source.data
            is ApngSource.File -> {
                fileSystem.read(source.path.toPath()) {
                    readByteArray()
                }
            }
            is ApngSource.Resource -> {
                // Try to load from classpath resources
                val resourceStream = this::class.java.getResourceAsStream("/${source.resourcePath}")
                    ?: fileSystem.read(source.resourcePath.toPath()) {
                        readByteArray()
                    }
                resourceStream?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("Resource not found: ${source.resourcePath}")
            }
            is ApngSource.Url -> {
                throw IllegalArgumentException(
                    "URL source not supported in resource loader. Use ApngLoader.loadFromUrl instead."
                )
            }
        }
    }
}

/**
 * Create the desktop resource loader.
 */
actual suspend fun createResourceLoader(): ApngResourceLoader {
    return DesktopApngResourceLoader()
}
