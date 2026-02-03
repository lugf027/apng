package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

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
