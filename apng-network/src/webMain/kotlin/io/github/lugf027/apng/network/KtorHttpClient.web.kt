package io.github.lugf027.apng.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout

/**
 * Create default Ktor HTTP client for Web (WASM/JS).
 * Uses auto-detected engine based on the platform.
 */
actual fun createDefaultHttpClient(
    connectTimeoutMs: Long,
    requestTimeoutMs: Long
): HttpClient {
    return HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeoutMs
        }
    }
}
