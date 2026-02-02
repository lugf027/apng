package io.github.lugf027.apng.network

/**
 * Interface for loading APNG resources from various sources.
 * Abstracts platform-specific resource loading mechanisms.
 */
interface ApngResourceLoader {
    /**
     * Load APNG data from the given source.
     *
     * @param source The data source (bytes, file, URL, or resource)
     * @return The APNG bytes
     * @throws Exception if loading fails
     */
    suspend fun load(source: ApngSource): ByteArray
}

/**
 * Create a platform-specific resource loader.
 * Each platform (Android, iOS, Desktop, Web) provides its own implementation.
 */
expect suspend fun createResourceLoader(): ApngResourceLoader
