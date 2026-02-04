package io.github.lugf027.apng.resources

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.github.lugf027.apng.compose.ApngComposition
import io.github.lugf027.apng.compose.ApngCompositionSpec

/**
 * Internal implementation of Resource-based [ApngCompositionSpec].
 *
 * Handles loading APNG data from Compose Multiplatform Resources using the provided
 * [readBytes] function (typically `Res::readBytes`).
 *
 * @property resourcePath The resource path relative to [directory]
 * @property readBytes The suspend function to read bytes from resources
 * @property directory The directory prefix in composeResources
 */
@Immutable
internal class ResourceCompositionSpec(
    private val resourcePath: String,
    private val readBytes: suspend (path: String) -> ByteArray,
    private val directory: String
) : ApngCompositionSpec {

    override val key: String
        get() = "resource_${directory}_$resourcePath"

    override suspend fun load(): ApngComposition {
        val fullPath = buildFullPath()
        val bytes = readBytes(fullPath)
        return ApngComposition.parse(bytes)
    }

    private fun buildFullPath(): String {
        val trimPath = resourcePath
            .removePrefix("/")
            .removeSuffix("/")
            .takeIf(String::isNotEmpty)

        return listOfNotNull(
            directory.takeIf(String::isNotEmpty),
            trimPath
        ).joinToString("/")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ResourceCompositionSpec

        if (resourcePath != other.resourcePath) return false
        if (readBytes != other.readBytes) return false
        if (directory != other.directory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resourcePath.hashCode()
        result = 31 * result + readBytes.hashCode()
        result = 31 * result + directory.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResourceCompositionSpec(path='$resourcePath', directory='$directory', key='$key')"
    }
}

/**
 * Create an [ApngCompositionSpec] that loads APNG from Compose Resources.
 *
 * Assets must be stored in the composeResources/[directory] directory.
 * Use `Res::readBytes` as the [readBytes] source.
 *
 * @param resourcePath The resource path relative to [directory]
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @param directory The directory in composeResources (default: "files")
 * @return An [ApngCompositionSpec] that loads from Compose Resources
 */
@Stable
public fun ApngCompositionSpec.Companion.Resource(
    resourcePath: String,
    readBytes: suspend (path: String) -> ByteArray,
    directory: String = "files"
): ApngCompositionSpec = ResourceCompositionSpec(
    resourcePath = resourcePath,
    readBytes = readBytes,
    directory = directory
)

/**
 * Create an [ApngCompositionSpec] from Compose Resources byte reader.
 *
 * Alternative API that provides more flexibility in path handling.
 * Use `Res::readBytes` as the [readBytes] source.
 *
 * @param path The resource path (full or relative)
 * @param directory The directory prefix (default: "files"). Empty if path is already full.
 * @param readBytes The function to read bytes from resources (typically `Res::readBytes`)
 * @return An [ApngCompositionSpec] that loads from Compose Resources
 */
@Stable
public fun ApngCompositionSpec.Companion.ResourceBytes(
    path: String,
    directory: String = "files",
    readBytes: suspend (path: String) -> ByteArray,
): ApngCompositionSpec = ResourceCompositionSpec(
    resourcePath = path,
    readBytes = readBytes,
    directory = directory
)
