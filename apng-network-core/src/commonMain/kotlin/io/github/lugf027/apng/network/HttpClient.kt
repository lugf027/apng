package io.github.lugf027.apng.network

/**
 * Interface for HTTP client implementations.
 * Allows custom HTTP client implementations for network requests.
 */
interface HttpClient {
    /**
     * Download data from the given URL with progress callback.
     *
     * @param url The URL to download from
     * @param onProgress Called with (downloaded, total) bytes. total may be 0 if unknown.
     * @return The downloaded bytes
     * @throws Exception if the download fails
     */
    suspend fun download(
        url: String,
        onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
    ): ByteArray
}
