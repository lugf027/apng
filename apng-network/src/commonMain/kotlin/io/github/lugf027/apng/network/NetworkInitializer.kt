package io.github.lugf027.apng.network

/**
 * Initialize the network module with Ktor HTTP client.
 * This should be called once at application startup.
 * 
 * Note: In most cases, you don't need to call this manually.
 * The [ApngCompositionSpec.Url] function will auto-initialize when needed.
 */
fun initializeApngNetwork() {
    // Initialize with Ktor HTTP client
    @Suppress("UNCHECKED_CAST")
    io.github.lugf027.apng.network.DefaultHttpClient = KtorHttpClient()
}
