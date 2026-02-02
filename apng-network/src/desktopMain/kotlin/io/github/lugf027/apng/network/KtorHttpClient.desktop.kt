package io.github.lugf027.apng.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout

/**
 * Create default Ktor HTTP client for Desktop (JVM) using OkHttp engine.
 */
actual fun createDefaultHttpClient(
    connectTimeoutMs: Long,
    requestTimeoutMs: Long
): HttpClient {
    return HttpClient(OkHttp) {
        install(HttpTimeout) {
            connectTimeoutMillis = connectTimeoutMs
            requestTimeoutMillis = requestTimeoutMs
        }
        engine {
            config {
                // Configure OkHttp client
                retryOnConnectionFailure(true)
            }
        }
    }
}
