package io.github.lugf027.apng.network

import io.github.lugf027.apng.core.ApngLoader

/**
 * Initialize the network module with Ktor HTTP client.
 * This should be called once at application startup.
 */
fun initializeApngNetwork() {
    // Initialize with Ktor HTTP client
    @Suppress("UNCHECKED_CAST")
    io.github.lugf027.apng.network.DefaultHttpClient = KtorHttpClient()
}

/**
 * Convenience extension to load APNG from URL in a single call.
 *
 * Example:
 * ```kotlin
 * val image = ApngLoader().loadFromUrl("https://example.com/anim.apng")
 * ```
 */
suspend fun ApngLoader.loadFromUrlSimple(
    url: String,
    onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
): io.github.lugf027.apng.core.ApngImage {
    // Make sure network is initialized
    if (io.github.lugf027.apng.network.DefaultHttpClient !is KtorHttpClient) {
        initializeApngNetwork()
    }
    return io.github.lugf027.apng.network.loadFromUrl(this, url, onProgress = onProgress)
}
