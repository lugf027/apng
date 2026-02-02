package io.github.lugf027.apng.resources

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.lugf027.apng.compose.ApngImage
import io.github.lugf027.apng.network.ApngSource
import io.github.lugf027.apng.network.getDefaultResourceLoader
import kotlinx.coroutines.launch

/**
 * Result of loading APNG bytes from resources.
 */
sealed class ResourceLoadResult {
    /** Loading in progress */
    data class Loading(val progress: Float = 0f) : ResourceLoadResult()
    
    /** Successfully loaded */
    data class Success(val bytes: ByteArray) : ResourceLoadResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return bytes.contentEquals(other.bytes)
        }
        override fun hashCode(): Int = bytes.contentHashCode()
    }
    
    /** Error during loading */
    data class Error(val exception: Exception) : ResourceLoadResult()
}

/**
 * Load and remember APNG bytes from a resource path.
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
 *         is ResourceLoadResult.Loading -> CircularProgressIndicator()
 *         is ResourceLoadResult.Success -> ApngImage(data = result.bytes, contentDescription = null)
 *         is ResourceLoadResult.Error -> Text("Failed to load: ${result.exception.message}")
 *     }
 * }
 * ```
 */
@Composable
fun rememberApngCompositionFromResource(
    resourcePath: String
): ResourceLoadResult {
    val result = remember { mutableStateOf<ResourceLoadResult>(ResourceLoadResult.Loading()) }

    LaunchedEffect(resourcePath) {
        result.value = ResourceLoadResult.Loading()
        
        launch {
            try {
                val resourceLoader = getDefaultResourceLoader()
                val bytes = resourceLoader.load(ApngSource.Resource(resourcePath))
                result.value = ResourceLoadResult.Success(bytes)
            } catch (e: Exception) {
                result.value = ResourceLoadResult.Error(e)
            }
        }
    }

    return result.value
}

/**
 * Load and remember APNG bytes from a local file path.
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
 *         is ResourceLoadResult.Loading -> CircularProgressIndicator()
 *         is ResourceLoadResult.Success -> ApngImage(data = result.bytes, contentDescription = null)
 *         is ResourceLoadResult.Error -> Text("Failed to load: ${result.exception.message}")
 *     }
 * }
 * ```
 */
@Composable
fun rememberApngCompositionFromFile(
    filePath: String
): ResourceLoadResult {
    val result = remember { mutableStateOf<ResourceLoadResult>(ResourceLoadResult.Loading()) }

    LaunchedEffect(filePath) {
        result.value = ResourceLoadResult.Loading()
        
        launch {
            try {
                val resourceLoader = getDefaultResourceLoader()
                val bytes = resourceLoader.load(ApngSource.File(filePath))
                result.value = ResourceLoadResult.Success(bytes)
            } catch (e: Exception) {
                result.value = ResourceLoadResult.Error(e)
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
    modifier: Modifier = Modifier,
    onLoading: @Composable () -> Unit = {
        CircularProgressIndicator()
    },
    onError: @Composable (Exception) -> Unit = { exception ->
        Text(
            "Failed to load: ${exception.message}",
            color = MaterialTheme.colorScheme.error
        )
    }
) {
    val result = rememberApngCompositionFromResource(resourcePath)
    
    when (result) {
        is ResourceLoadResult.Loading -> onLoading()
        is ResourceLoadResult.Success -> {
            ApngImage(
                data = result.bytes,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        is ResourceLoadResult.Error -> onError(result.exception)
    }
}

/**
 * Simplified Composable to display APNG from a local file with built-in loading and error states.
 */
@Composable
fun ApngImageFromFile(
    filePath: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    onLoading: @Composable () -> Unit = {
        CircularProgressIndicator()
    },
    onError: @Composable (Exception) -> Unit = { exception ->
        Text(
            "Failed to load: ${exception.message}",
            color = MaterialTheme.colorScheme.error
        )
    }
) {
    val result = rememberApngCompositionFromFile(filePath)
    
    when (result) {
        is ResourceLoadResult.Loading -> onLoading()
        is ResourceLoadResult.Success -> {
            ApngImage(
                data = result.bytes,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        is ResourceLoadResult.Error -> onError(result.exception)
    }
}
