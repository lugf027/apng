package io.github.lugf027.apng.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Create default Ktor HTTP client for Desktop (JVM) using OkHttp engine.
 */
actual fun createDefaultHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                // Configure OkHttp client
                retryOnConnectionFailure(true)
            }
        }
    }
}
