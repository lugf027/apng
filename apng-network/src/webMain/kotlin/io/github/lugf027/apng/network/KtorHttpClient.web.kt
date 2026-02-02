package io.github.lugf027.apng.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

/**
 * Create default Ktor HTTP client for Web (WASM/JS) using JS engine.
 */
actual fun createDefaultHttpClient(): HttpClient {
    return HttpClient(Js) {
        engine {
            // Configure JS engine for browser Fetch API
        }
    }
}
