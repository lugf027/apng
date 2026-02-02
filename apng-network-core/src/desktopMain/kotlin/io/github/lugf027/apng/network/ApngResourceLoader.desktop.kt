package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.io.InputStream

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
                val resourceStream: InputStream? = this::class.java.getResourceAsStream("/${source.resourcePath}")
                if (resourceStream != null) {
                    resourceStream.use { stream -> stream.readBytes() }
                } else {
                    // Fallback to file system
                    fileSystem.read(source.resourcePath.toPath()) {
                        readByteArray()
                    }
                }
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

/**
 * Get the default cache directory for desktop platform.
 */
internal actual fun getDefaultCacheDirectory(): Path {
    val tmpDir = System.getProperty("java.io.tmpdir") ?: "/tmp"
    return "$tmpDir/apng-cache".toPath()
}

/**
 * Get the system file system for desktop (JVM).
 */
internal actual fun getSystemFileSystem(): FileSystem = FileSystem.SYSTEM
