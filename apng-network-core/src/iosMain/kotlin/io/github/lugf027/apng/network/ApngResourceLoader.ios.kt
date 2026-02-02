package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of ApngResourceLoader.
 * Loads resources from app bundle using NSBundle and file system using Okio.
 */
internal class IosApngResourceLoader : ApngResourceLoader {
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
                val mainBundle = NSBundle.mainBundle
                val resourceName = source.resourcePath.substringBeforeLast(".")
                val resourceExt = source.resourcePath.substringAfterLast(".", "")

                val resourcePath = if (resourceExt.isNotEmpty()) {
                    mainBundle.pathForResource(resourceName, ofType = resourceExt)
                } else {
                    mainBundle.pathForResource(resourceName, ofType = null)
                }

                if (resourcePath != null) {
                    fileSystem.read(resourcePath.toPath()) {
                        readByteArray()
                    }
                } else {
                    throw IllegalArgumentException("Resource not found in bundle: ${source.resourcePath}")
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
 * Create the iOS resource loader.
 */
actual suspend fun createResourceLoader(): ApngResourceLoader {
    return IosApngResourceLoader()
}

/**
 * Get the default cache directory for iOS platform.
 */
internal actual fun getDefaultCacheDirectory(): Path {
    val paths = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    )
    val cachesDir = paths.firstOrNull() as? String ?: "/tmp"
    return "$cachesDir/apng-cache".toPath()
}

/**
 * Get the system file system for iOS (Native).
 */
internal actual fun getSystemFileSystem(): FileSystem = FileSystem.SYSTEM
