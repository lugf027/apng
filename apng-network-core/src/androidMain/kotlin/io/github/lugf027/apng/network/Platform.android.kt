package io.github.lugf027.apng.network

import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

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
