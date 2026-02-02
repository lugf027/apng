package io.github.lugf027.apng.network

/**
 * Represents different sources of APNG data.
 * Supports loading from bytes, files, network URLs, and Compose Resources.
 */
sealed interface ApngSource {
    /**
     * Load APNG from in-memory byte array
     */
    data class Bytes(val data: ByteArray) : ApngSource {
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
     * Load APNG from local file path
     */
    data class File(val path: String) : ApngSource

    /**
     * Load APNG from network URL with optional caching
     */
    data class Url(val url: String) : ApngSource

    /**
     * Load APNG from Compose Resources
     */
    data class Resource(val resourcePath: String) : ApngSource
}
