package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.lugf027.apng.core.ApngImage
import io.github.lugf027.apng.core.ApngLoader

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
        try {
            val loader = ApngLoader()
            val apngImage = loader.loadFromBytes(data)
            state = ApngLoadState.Success(apngImage)
        } catch (e: Throwable) {
            state = ApngLoadState.Error(e)
        }
    }

    return state
}
