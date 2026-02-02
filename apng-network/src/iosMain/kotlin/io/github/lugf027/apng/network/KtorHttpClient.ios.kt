package io.github.lugf027.apng.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/**
 * Create default Ktor HTTP client for iOS using Darwin (NSURLSession) engine.
 */
actual fun createDefaultHttpClient(): HttpClient {
    return HttpClient(Darwin) {
        engine {
            // Configure Darwin engine
        }
    }
}
