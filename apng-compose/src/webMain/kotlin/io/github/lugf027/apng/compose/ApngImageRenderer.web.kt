package io.github.lugf027.apng.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.lugf027.apng.core.ApngImage

/**
 * Web 平台的 APNG 渲染器实现
 * 目前显示占位符，后续可以使用 Canvas API 实现
 */
@Composable
actual fun ApngImageRenderer(
    apngImage: ApngImage,
    rawData: ByteArray,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    autoPlay: Boolean
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Web 平台暂时显示占位符
        // TODO: 使用 HTML5 Canvas API 实现图像渲染
        Text("APNG: ${apngImage.width}x${apngImage.height}")
    }
}
