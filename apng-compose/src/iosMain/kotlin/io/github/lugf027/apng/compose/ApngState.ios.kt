package io.github.lugf027.apng.compose

import okio.FileSystem
import okio.Path.Companion.toPath

actual suspend fun loadFileData(path: String): ByteArray {
    return try {
        // Use Okio for cross-platform file I/O
        // This works with iOS FileSystem which uses Foundation APIs under the hood
        val fileSystem = FileSystem.SYSTEM
        val filePath = path.toPath()
        fileSystem.read(filePath) {
            readByteArray()
        }
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to load file: $path", e)
    }
}
