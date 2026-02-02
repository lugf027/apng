package io.github.lugf027.apng.compose

import android.content.Context
import java.io.File

actual suspend fun loadFileData(path: String): ByteArray {
    return try {
        File(path).readBytes()
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to load file: $path", e)
    }
}
