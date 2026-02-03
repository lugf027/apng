package io.github.lugf027.apng.network

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

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
