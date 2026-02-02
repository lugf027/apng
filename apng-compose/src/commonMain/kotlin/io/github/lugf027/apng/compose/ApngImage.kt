package io.github.lugf027.apng.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.lugf027.apng.core.ApngImage
import io.github.lugf027.apng.core.AnimationController
import io.github.lugf027.apng.core.ApngLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

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

    if (error != null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // Error state - could show error icon or text
        }
    } else if (isLoading) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        apngImage?.let { image ->
            ApngImageContent(
                apngImage = image,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                autoPlay = autoPlay
            )
        }
    }
}

/**
 * APNG 内容显示组件
 */
@Composable
private fun ApngImageContent(
    apngImage: ApngImage,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    autoPlay: Boolean
) {
    val controller = remember { AnimationController(apngImage) }
    var frameIndex by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(autoPlay) {
        if (autoPlay && apngImage.isAnimated) {
            controller.play()
            while (controller.playing) {
                delay(controller.getCurrentFrameDelay())
                controller.nextFrame()
                frameIndex = controller.currentFrame?.index ?: 0
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.stop()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Simple placeholder - actual image rendering would use platform-specific painters
        androidx.compose.material3.Text("APNG: ${apngImage.width}x${apngImage.height}")
    }
}

