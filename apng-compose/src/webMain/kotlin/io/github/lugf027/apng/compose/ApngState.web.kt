package io.github.lugf027.apng.compose

actual suspend fun loadFileData(path: String): ByteArray {
    return try {
        // Web file loading via fetch API
        // This would use JS interop to call fetch()
        ByteArray(0) // Placeholder - Web cannot access local files
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to load file: $path", e)
    }
}
