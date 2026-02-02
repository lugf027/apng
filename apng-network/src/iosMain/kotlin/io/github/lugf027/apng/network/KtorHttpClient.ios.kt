package io.github.lugf027.apng.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout

/**
 * Create default Ktor HTTP client for iOS using Darwin (NSURLSession) engine.
 */
actual fun createDefaultHttpClient(
    connectTimeoutMs: Long,
    requestTimeoutMs: Long
): HttpClient {
    return HttpClient(Darwin) {
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeoutMs
            socketTimeoutMillis = requestTimeoutMs
        }
        engine {
            // Configure Darwin engine
        }
    }
}
