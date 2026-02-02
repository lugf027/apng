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
 * APNG 加载状态
 */
sealed class ApngLoadState {
    object Loading : ApngLoadState()
    data class Success(val apngImage: ApngImage) : ApngLoadState()
    data class Error(val throwable: Throwable) : ApngLoadState()
}

/**
 * APNG 状态管理器
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

/**
 * APNG 状态管理器（文件路径版本）
 */
@Composable
fun rememberApngStateFromPath(path: String): ApngLoadState {
    var state: ApngLoadState by remember { mutableStateOf(ApngLoadState.Loading) }

    LaunchedEffect(path) {
        try {
            val data = loadFileData(path)
            val loader = ApngLoader()
            val apngImage = loader.loadFromBytes(data)
            state = ApngLoadState.Success(apngImage)
        } catch (e: Throwable) {
            state = ApngLoadState.Error(e)
        }
    }

    return state
}

/**
 * 平台特定的文件加载函数
 */
expect suspend fun loadFileData(path: String): ByteArray
