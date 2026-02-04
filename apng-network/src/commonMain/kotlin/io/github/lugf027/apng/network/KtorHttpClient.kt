package io.github.lugf027.apng.network

import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.delay
import kotlinx.io.readByteArray

/**
 * Ktor-based HTTP client for downloading APNG files.
 * Supports automatic retries and progress callbacks.
 */
class KtorHttpClient(
    private val client: HttpClient? = null,
    private val maxRetries: Int = 3,
    private val connectTimeoutMs: Long = 15000,
    private val requestTimeoutMs: Long = 15000
) : io.github.lugf027.apng.network.HttpClient {

    private val httpClient: HttpClient by lazy {
        client ?: createDefaultHttpClient(connectTimeoutMs, requestTimeoutMs)
    }

    override suspend fun download(
        url: String,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): ByteArray {
        ApngLogger.d(ApngLogTags.NETWORK, "KtorHttpClient: Starting download from $url")
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                if (attempt > 0) {
                    ApngLogger.d(ApngLogTags.NETWORK) { "KtorHttpClient: Retry attempt ${attempt + 1}/$maxRetries for $url" }
                }
                
                val response: HttpResponse = httpClient.get(url) {
                    timeout {
                        connectTimeoutMillis = connectTimeoutMs
                        requestTimeoutMillis = requestTimeoutMs
                    }
                }

                if (!response.status.isSuccess()) {
                    ApngLogger.w(ApngLogTags.NETWORK) { "KtorHttpClient: HTTP error ${response.status.value} for $url" }
                    throw HttpException(
                        "HTTP ${response.status.value}: ${response.status.description}",
                        response.status.value
                    )
                }

                val contentLength = response.contentLength() ?: 0L
                ApngLogger.v(ApngLogTags.NETWORK) { "KtorHttpClient: Content-Length=$contentLength for $url" }
                onProgress(0, contentLength)

                val bytes = response.bodyAsChannel().readRemaining().readByteArray()
                onProgress(bytes.size.toLong(), contentLength)

                ApngLogger.i(ApngLogTags.NETWORK) { "KtorHttpClient: Downloaded ${bytes.size} bytes from $url" }
                return bytes

            } catch (e: Exception) {
                lastException = e
                ApngLogger.w(ApngLogTags.NETWORK, "KtorHttpClient: Download attempt ${attempt + 1} failed for $url: ${e.message}")
                if (attempt < maxRetries - 1) {
                    val delayMs = (1000L * (attempt + 1)) // Exponential backoff: 1s, 2s, 3s
                    ApngLogger.d(ApngLogTags.NETWORK) { "KtorHttpClient: Retrying in ${delayMs}ms" }
                    delay(delayMs)
                }
            }
        }

        ApngLogger.e(ApngLogTags.NETWORK, "KtorHttpClient: All retries failed for $url", lastException)
        throw lastException ?: Exception("Failed to download from $url after $maxRetries retries")
    }
}

/**
 * HTTP exception for network errors.
 */
class HttpException(message: String, val statusCode: Int) : Exception(message)

/**
 * Create default HTTP client with platform-specific configuration.
 */
expect fun createDefaultHttpClient(
    connectTimeoutMs: Long = 15000,
    requestTimeoutMs: Long = 15000
): HttpClient

/**
 * Check if HTTP status is successful (2xx).
 */
private fun HttpStatusCode.isSuccess(): Boolean = value in 200..299
