package io.github.lugf027.apng.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.lugf027.apng.core.ApngImage
import io.github.lugf027.apng.core.ApngLoader

/**
 * APNG 图像显示组件
 * 支持动画播放和自动播放控制
 */
@Composable
fun ApngImage(
    data: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    autoPlay: Boolean = true,
    onError: ((Throwable) -> Unit)? = null
) {
    var apngImage: ApngImage? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error: Throwable? by remember { mutableStateOf(null) }

    LaunchedEffect(data) {
        try {
            val loader = ApngLoader()
            apngImage = loader.loadFromBytes(data)
            isLoading = false
        } catch (e: Throwable) {
            error = e
            isLoading = false
            onError?.invoke(e)
        }
    }

    when {
        error != null -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${error?.message}")
            }
        }
        isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        else -> {
            apngImage?.let { image ->
                ApngImageRenderer(
                    apngImage = image,
                    rawData = data,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale,
                    autoPlay = autoPlay
                )
            }
        }
    }
}

/**
 * 平台特定的 APNG 渲染器
 */
@Composable
expect fun ApngImageRenderer(
    apngImage: ApngImage,
    rawData: ByteArray,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    autoPlay: Boolean
)
