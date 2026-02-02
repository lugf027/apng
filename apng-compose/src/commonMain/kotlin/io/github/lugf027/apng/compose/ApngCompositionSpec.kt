package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import io.github.lugf027.apng.core.ApngImage

/**
 * APNG composition spec for loading from various sources.
 * Provides a unified way to specify APNG sources (bytes, file, URL, resource).
 */
sealed class ApngCompositionSpec {
    /**
     * Load from in-memory byte array
     */
    data class Bytes(val data: ByteArray) : ApngCompositionSpec() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    /**
     * Load from local file path
     */
    data class File(val path: String) : ApngCompositionSpec()

    /**
     * Load from network URL
     */
    data class Url(val url: String) : ApngCompositionSpec()

    /**
     * Load from Compose Resources
     */
    data class Resource(val resourcePath: String) : ApngCompositionSpec()
}

/**
 * Result of async APNG composition loading
 */
sealed interface ApngCompositionLoadResult {
    data class Loading(val progress: Float? = null) : ApngCompositionLoadResult
    data class Success(val composition: ApngImage) : ApngCompositionLoadResult
    data class Error(val exception: Exception) : ApngCompositionLoadResult
}

/**
 * Remember and load APNG from various sources.
 *
 * @param spec The composition spec (URL, File, Resource, or Bytes)
 * @return The loading result (Loading, Success, or Error)
 */
@Composable
expect fun rememberApngCompositionSpec(
    spec: ApngCompositionSpec
): ApngCompositionLoadResult
