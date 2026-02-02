package io.github.lugf027.apng.network

import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Android implementation of ApngResourceLoader.
 * Loads resources from app assets, external storage, or provided bytes.
 */
internal class AndroidApngResourceLoader(
    private val context: Context
) : ApngResourceLoader {
    override suspend fun load(source: ApngSource): ByteArray {
        return when (source) {
            is ApngSource.Bytes -> source.data
            is ApngSource.File -> {
                FileSystem.SYSTEM.read(source.path.toPath()) {
                    readByteArray()
                }
            }
            is ApngSource.Resource -> {
                // Try to load from assets first
                try {
                    context.assets.open(source.resourcePath).use { input ->
                        input.readBytes()
                    }
                } catch (e: Exception) {
                    // Fall back to file system
                    FileSystem.SYSTEM.read(source.resourcePath.toPath()) {
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
 * Create the Android resource loader.
 * Uses the application context to access assets and storage.
 */
actual suspend fun createResourceLoader(): ApngResourceLoader {
    // Get application context from Android runtime
    val context = getApplicationContext()
    return AndroidApngResourceLoader(context)
}

/**
 * Helper to get the application context.
 * Works with Android lifecycle to get the current application context.
 */
private fun getApplicationContext(): Context {
    return try {
        Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? Context
            ?: throw Exception("Could not get application context")
    } catch (e: Exception) {
        throw IllegalStateException(
            "Cannot access Android context. Make sure this is called from Android application context.",
            e
        )
    }
}

/**
 * Get the default cache directory for Android platform.
 */
internal actual fun getDefaultCacheDirectory(): Path {
    return try {
        val context = getApplicationContext()
        val cacheDir = context.cacheDir
        "${cacheDir.absolutePath}/apng-cache".toPath()
    } catch (e: Exception) {
        // Fallback to temp directory
        "/data/local/tmp/apng-cache".toPath()
    }
}

/**
 * Get the system file system for Android.
 */
internal actual fun getSystemFileSystem(): FileSystem = FileSystem.SYSTEM
