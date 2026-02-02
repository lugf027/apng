package io.github.lugf027.apng.network

import android.content.Context
import androidx.core.content.ContextCompat
import okio.FileSystem
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
