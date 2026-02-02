package io.github.lugf027.apng.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.lugf027.apng.core.ApngLoader
import io.github.lugf027.apng.network.ApngSource
import io.github.lugf027.apng.network.getDefaultResourceLoader
import kotlinx.coroutines.launch

@Composable
actual fun rememberApngCompositionSpec(
    spec: ApngCompositionSpec
): ApngCompositionLoadResult {
    val result = remember { mutableStateOf<ApngCompositionLoadResult>(ApngCompositionLoadResult.Loading()) }

    LaunchedEffect(spec) {
        result.value = ApngCompositionLoadResult.Loading()
        
        launch {
            try {
                val bytes = when (spec) {
                    is ApngCompositionSpec.Bytes -> spec.data
                    is ApngCompositionSpec.File -> {
                        val resourceLoader = getDefaultResourceLoader()
                        resourceLoader.load(ApngSource.File(spec.path))
                    }
                    is ApngCompositionSpec.Resource -> {
                        val resourceLoader = getDefaultResourceLoader()
                        resourceLoader.load(ApngSource.Resource(spec.resourcePath))
                    }
                    is ApngCompositionSpec.Url -> {
                        val loader = ApngLoader()
                        val image = loader.loadFromUrl(spec.url)
                        result.value = ApngCompositionLoadResult.Success(image)
                        return@launch
                    }
                }
                
                val image = ApngLoader().loadFromBytes(bytes)
                result.value = ApngCompositionLoadResult.Success(image)
            } catch (e: Exception) {
                result.value = ApngCompositionLoadResult.Error(e)
            }
        }
    }

    return result.value
}
