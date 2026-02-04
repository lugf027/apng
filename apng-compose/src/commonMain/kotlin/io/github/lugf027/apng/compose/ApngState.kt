package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.lugf027.apng.core.ApngImage
import io.github.lugf027.apng.core.ApngLoader
import io.github.lugf027.apng.logger.ApngLogTags
import io.github.lugf027.apng.logger.ApngLogger

/**
 * APNG loading state
 */
sealed class ApngLoadState {
    data object Loading : ApngLoadState()
    data class Success(val apngImage: ApngImage) : ApngLoadState()
    data class Error(val throwable: Throwable) : ApngLoadState()
}

/**
 * APNG state manager - loads from byte array
 */
@Composable
fun rememberApngState(data: ByteArray): ApngLoadState {
    var state: ApngLoadState by remember { mutableStateOf(ApngLoadState.Loading) }

    LaunchedEffect(data) {
        ApngLogger.d(ApngLogTags.COMPOSE, "rememberApngState: Loading APNG from ${data.size} bytes")
        try {
            val loader = ApngLoader()
            val apngImage = loader.loadFromBytes(data)
            state = ApngLoadState.Success(apngImage)
            ApngLogger.i(ApngLogTags.COMPOSE) { "rememberApngState: Successfully loaded APNG ${apngImage.width}x${apngImage.height}" }
        } catch (e: Throwable) {
            state = ApngLoadState.Error(e)
            ApngLogger.e(ApngLogTags.COMPOSE, "rememberApngState: Failed to load APNG", e)
        }
    }

    return state
}
