package io.github.lugf027.apng.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * APNG 图像显示组件
 * 
 * 使用 Compose Painter 进行渲染，参考 compottie 的设计理念
 * 
 * @param data APNG 图像数据
 * @param contentDescription 内容描述（用于无障碍）
 * @param modifier 修饰符
 * @param contentScale 内容缩放方式
 * @param autoPlay 是否自动播放动画
 * @param speed 播放速度倍率
 * @param iterations 循环次数，0 表示无限循环
 * @param onError 错误回调
 */
@Composable
fun ApngImage(
    data: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0,
    onError: ((Throwable) -> Unit)? = null
) {
    val compositionResult = rememberApngComposition(data)
    
    when (compositionResult) {
        is ApngCompositionResult.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        is ApngCompositionResult.Error -> {
            onError?.invoke(compositionResult.throwable)
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${compositionResult.throwable.message}")
            }
        }
        is ApngCompositionResult.Success -> {
            val painter = rememberApngPainter(
                composition = compositionResult.composition,
                autoPlay = autoPlay,
                speed = speed,
                iterations = iterations
            )
            
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}

/**
 * APNG 图像显示组件 - 使用预加载的合成数据
 * 
 * @param composition APNG 合成数据
 * @param contentDescription 内容描述（用于无障碍）
 * @param modifier 修饰符
 * @param contentScale 内容缩放方式
 * @param autoPlay 是否自动播放动画
 * @param speed 播放速度倍率
 * @param iterations 循环次数，0 表示无限循环
 */
@Composable
fun ApngImage(
    composition: ApngComposition,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    autoPlay: Boolean = true,
    speed: Float = 1f,
    iterations: Int = 0
) {
    val painter = rememberApngPainter(
        composition = composition,
        autoPlay = autoPlay,
        speed = speed,
        iterations = iterations
    )
    
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * APNG 图像显示组件 - 使用自定义进度控制
 * 
 * @param composition APNG 合成数据
 * @param progress 当前播放进度的 lambda（0.0-1.0）
 * @param contentDescription 内容描述（用于无障碍）
 * @param modifier 修饰符
 * @param contentScale 内容缩放方式
 */
@Composable
fun ApngImage(
    composition: ApngComposition?,
    progress: () -> Float,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val painter = rememberApngPainter(
        composition = composition,
        progress = progress
    )
    
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
