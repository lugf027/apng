package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

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
