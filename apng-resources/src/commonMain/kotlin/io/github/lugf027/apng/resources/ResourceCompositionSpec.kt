package io.github.lugf027.apng.resources

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.github.lugf027.apng.compose.ApngComposition
import io.github.lugf027.apng.compose.ApngCompositionSpec

/**
 * Create an [ApngCompositionSpec] that loads from Compose Resources.
 * 
 * Assets must be stored in the composeResources/files/ directory.
 * 
 * Example:
 * ```kotlin
 * val composition by rememberApngComposition {
 *     ApngCompositionSpec.Resource(
 *         resourcePath = "animation.apng",
 *         readBytes = Res::readBytes
 *     )
 * }
 * ```
 * 
 * @param resourcePath The resource path relative to files directory
 * @param readBytes The function to read bytes from resources (typically Res::readBytes)
 * @param directory The directory in composeResources (default: "files")
 * @return An [ApngCompositionSpec] that loads from resources
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
 * Internal implementation of Resource-based ApngCompositionSpec.
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
        return "Resource(path='$resourcePath', directory='$directory', key=$key)"
    }
}
