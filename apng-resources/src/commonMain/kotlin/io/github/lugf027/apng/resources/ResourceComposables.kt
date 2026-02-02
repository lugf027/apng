package io.github.lugf027.apng.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.lugf027.apng.compose.ApngImage
import io.github.lugf027.apng.core.ApngImage as ApngImageData
import io.github.lugf027.apng.core.ApngLoader
import io.github.lugf027.apng.network.ApngCompositionResult
import io.github.lugf027.apng.network.ApngSource
import io.github.lugf027.apng.network.getDefaultResourceLoader
import kotlinx.coroutines.launch

/**
 * Load and remember APNG from a local file path or Compose Resources.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val result = rememberApngCompositionFromResource(
 *         resourcePath = "files/animation.apng"
 *     )
 *     
 *     when (result) {
 *         is ApngCompositionResult.Loading -> CircularProgressIndicator()
 *         is ApngCompositionResult.Success -> ApngImage(result.composition)
 *         is ApngCompositionResult.Error -> Text("Failed to load: ${result.exception.message}")
 *     }
 * }
 * ```
 */
@Composable
fun rememberApngCompositionFromResource(
    resourcePath: String
): ApngCompositionResult {
    val result = remember { mutableStateOf<ApngCompositionResult>(ApngCompositionResult.Loading()) }

    LaunchedEffect(resourcePath) {
        result.value = ApngCompositionResult.Loading()
        
        launch {
            try {
                val resourceLoader = getDefaultResourceLoader()
                val bytes = resourceLoader.load(ApngSource.Resource(resourcePath))
                val apngImage = ApngLoader().loadFromBytes(bytes)
                result.value = ApngCompositionResult.Success(apngImage)
            } catch (e: Exception) {
                result.value = ApngCompositionResult.Error(e)
            }
        }
    }

    return result.value
}

/**
 * Load and remember APNG from a local file path.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val result = rememberApngCompositionFromFile(
 *         filePath = "/path/to/animation.apng"
 *     )
 *     
 *     when (result) {
 *         is ApngCompositionResult.Loading -> CircularProgressIndicator()
 *         is ApngCompositionResult.Success -> ApngImage(result.composition)
 *         is ApngCompositionResult.Error -> Text("Failed to load: ${result.exception.message}")
 *     }
 * }
 * ```
 */
@Composable
fun rememberApngCompositionFromFile(
    filePath: String
): ApngCompositionResult {
    val result = remember { mutableStateOf<ApngCompositionResult>(ApngCompositionResult.Loading()) }

    LaunchedEffect(filePath) {
        result.value = ApngCompositionResult.Loading()
        
        launch {
            try {
                val resourceLoader = getDefaultResourceLoader()
                val bytes = resourceLoader.load(ApngSource.File(filePath))
                val apngImage = ApngLoader().loadFromBytes(bytes)
                result.value = ApngCompositionResult.Success(apngImage)
            } catch (e: Exception) {
                result.value = ApngCompositionResult.Error(e)
            }
        }
    }

    return result.value
}

/**
 * Simplified Composable to display APNG from resources with built-in loading and error states.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     ApngImageFromResource(
 *         resourcePath = "files/animation.apng",
 *         contentDescription = "Loading animation"
 *     )
 * }
 * ```
 */
@Composable
fun ApngImageFromResource(
    resourcePath: String,
    contentDescription: String? = null,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    onLoading: @Composable () -> Unit = {
        androidx.compose.material3.CircularProgressIndicator()
    },
    onError: @Composable (Exception) -> Unit = { exception ->
        androidx.compose.material3.Text(
            "Failed to load: ${exception.message}",
            color = androidx.compose.material3.MaterialTheme.colorScheme.error
        )
    }
) {
    val result = rememberApngCompositionFromResource(resourcePath)
    
    when (result) {
        is ApngCompositionResult.Loading -> onLoading()
        is ApngCompositionResult.Success -> {
            ApngImage(
                data = result.composition,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        is ApngCompositionResult.Error -> onError(result.exception)
    }
}

/**
 * Simplified Composable to display APNG from a local file with built-in loading and error states.
 */
@Composable
fun ApngImageFromFile(
    filePath: String,
    contentDescription: String? = null,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    onLoading: @Composable () -> Unit = {
        androidx.compose.material3.CircularProgressIndicator()
    },
    onError: @Composable (Exception) -> Unit = { exception ->
        androidx.compose.material3.Text(
            "Failed to load: ${exception.message}",
            color = androidx.compose.material3.MaterialTheme.colorScheme.error
        )
    }
) {
    val result = rememberApngCompositionFromFile(filePath)
    
    when (result) {
        is ApngCompositionResult.Loading -> onLoading()
        is ApngCompositionResult.Success -> {
            ApngImage(
                data = result.composition,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        is ApngCompositionResult.Error -> onError(result.exception)
    }
}
