# APNG Kotlin Multiplatform Library

[中文版本](./README_zh.md)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.0-brightgreen.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android API](https://img.shields.io/badge/Android%20API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lugf027/apng-core.svg)](https://central.sonatype.com/search?q=io.github.lugf027)

A high-performance, modular Kotlin Multiplatform APNG (Animated PNG) parsing and rendering library with support for network loading, caching, and cross-platform resource management.

## Features

- ✅ **APNG Parsing & Rendering**: Full support for APNG animation format
- ✅ **Multiplatform Support**: Android, iOS, Desktop (JVM), and Web (WASM/JS)
- ✅ **Network Loading**: Download and cache APNG files from URLs
- ✅ **LRU Cache**: Automatic disk-based caching with size limits
- ✅ **Progress Callbacks**: Track download progress in real-time
- ✅ **Auto Retry**: Automatic retry with exponential backoff
- ✅ **Compose Integration**: Native Jetpack Compose Multiplatform support
- ✅ **Multiple Data Sources**: Load from bytes, files, URLs, or resources
- ✅ **Error Handling**: Comprehensive error handling and fallbacks

![](./docs/images/demo_desktop.png)

## Architecture

The library is organized into modular components:

```
apng-core/              # Core APNG parsing and rendering (no UI)
│
├─ apng-network-core/   # Network loading infrastructure
│  ├─ ApngSource        # Data source abstraction
│  ├─ ApngResourceLoader# Platform-specific resource loading
│  ├─ ApngCacheStrategy # Caching interface
│  ├─ DiskLruCache      # LRU cache implementation
│  └─ HttpClient        # HTTP client interface
│
├─ apng-network/        # Network loading implementation
│  ├─ KtorHttpClient    # Ktor-based HTTP client
│  └─ Composables       # Network loading Composables
│
├─ apng-resources/      # Compose Resources support
│  └─ Composables       # Resource loading Composables
│
└─ apng-compose/        # Compose UI components
   ├─ ApngImage         # Main composable
   ├─ ApngPainter       # Painter implementation
   └─ ApngCompositionSpec # Multi-source loading
```

## Quick Start

### Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core library
    implementation("io.github.lugf027:apng-core:1.0.0")
    
    // UI components
    implementation("io.github.lugf027:apng-compose:1.0.0")
    
    // Network loading (optional)
    implementation("io.github.lugf027:apng-network:1.0.0")
    
    // Resource loading (optional)
    implementation("io.github.lugf027:apng-resources:1.0.0")
}
```

### Basic Usage - Load from Byte Array

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.lugf027.apng.compose.ApngImage
import io.github.lugf027.apng.compose.ApngCompositionSpec
import io.github.lugf027.apng.compose.rememberApngCompositionSpec
import io.github.lugf027.apng.compose.ApngCompositionLoadResult

@Composable
fun DisplayApng(apngBytes: ByteArray) {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.Bytes(apngBytes)
    )
    
    when (result) {
        is ApngCompositionLoadResult.Loading -> {
            CircularProgressIndicator()
        }
        is ApngCompositionLoadResult.Success -> {
            ApngImage(
                composition = result.composition,
                contentDescription = "My APNG",
                modifier = Modifier.size(200.dp)
            )
        }
        is ApngCompositionLoadResult.Error -> {
            Text("Error: ${result.exception.message}")
        }
    }
}
```

### Load from URL (with Network Support)

```kotlin
import io.github.lugf027.apng.network.rememberApngCompositionFromUrl

@Composable
fun DisplayApngFromUrl() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    when (result) {
        is ApngCompositionResult.Loading -> {
            // Display progress
            val progress = result.progress ?: 0f
            LinearProgressIndicator(progress)
        }
        is ApngCompositionResult.Success -> {
            ApngImage(result.composition, "Loaded from URL")
        }
        is ApngCompositionResult.Error -> {
            Text("Download failed: ${result.exception.message}")
        }
    }
}
```

### Load from Local File

```kotlin
import io.github.lugf027.apng.compose.ApngCompositionSpec

@Composable
fun DisplayApngFromFile(filePath: String) {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.File(filePath)
    )
    // Handle result...
}
```

### Load from Compose Resources

```kotlin
import io.github.lugf027.apng.compose.ApngCompositionSpec

@Composable
fun DisplayApngFromResources() {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.Resource("drawable/animation.apng")
    )
    // Handle result...
}
```

## Data Sources

```kotlin
// Byte array
ApngCompositionSpec.Bytes(data)

// Local file
ApngCompositionSpec.File(path)

// Network URL
ApngCompositionSpec.Url(url)

// Compose Resources
ApngCompositionSpec.Resource(resourcePath)
```

## Network Loading

### Network Configuration

Initialize at app startup:

```kotlin
import io.github.lugf027.apng.network.initializeApngNetwork
import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize network module
        initializeApngNetwork()
    }
}
```

### Custom Configuration

```kotlin
import io.github.lugf027.apng.network.KtorHttpClient
import io.github.lugf027.apng.network.DiskApngCacheStrategy
import io.github.lugf027.apng.network.DefaultHttpClient
import io.github.lugf027.apng.network.DefaultCacheStrategy

fun setupNetwork() {
    // Configure HTTP client
    DefaultHttpClient = KtorHttpClient(
        maxRetries = 5,              // Maximum retry attempts
        connectTimeoutMs = 30000,    // Connection timeout
        requestTimeoutMs = 60000     // Request timeout
    )
    
    // Use default cache (can be customized)
    // DefaultCacheStrategy = DiskApngCacheStrategy.Instance
}
```

### Progress Tracking

```kotlin
@Composable
fun LoadWithProgress() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    when (result) {
        is ApngCompositionResult.Loading -> {
            val progress = result.progress ?: 0f
            LinearProgressIndicator(progress)
        }
        is ApngCompositionResult.Success -> ApngImage(result.composition, null)
        is ApngCompositionResult.Error -> Text("Error")
    }
}
```

## Caching

The library automatically caches downloaded APNG files using LRU (Least Recently Used) strategy:

```kotlin
import io.github.lugf027.apng.network.DiskApngCacheStrategy
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

val cache = DiskApngCacheStrategy.Instance

// Check if URL is cached
val cachedPath = cache.path("https://example.com/animation.apng")
if (cachedPath != null) {
    println("Found in cache: $cachedPath")
}

// Clear all cache
lifecycleScope.launch {
    cache.clear()
    println("Cache cleared")
}
```

**Default cache locations:**
- Android: `context.cacheDir/apng-cache`
- iOS: App Documents directory
- Desktop: System temp directory
- Web: Browser IndexedDB (not persisted)

**Default cache size:** 100 MB

## Error Handling

```kotlin
@Composable
fun HandleErrors() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    when (result) {
        is ApngCompositionResult.Error -> {
            when (result.exception) {
                is HttpException -> {
                    val statusCode = (result.exception as HttpException).statusCode
                    Text("HTTP Error: $statusCode")
                }
                is java.io.IOException -> {
                    Text("Network error. Check your connection.")
                }
                else -> {
                    Text("Error: ${result.exception.message}")
                }
            }
        }
        else -> {}
    }
}
```

## Imperative API (Coroutines)

```kotlin
import io.github.lugf027.apng.core.ApngLoader

lifecycleScope.launch {
    try {
        // Load directly from URL
        val apngImage = ApngLoader().loadFromUrl(
            url = "https://example.com/animation.apng",
            onProgress = { downloaded, total ->
                println("Progress: $downloaded / $total bytes")
            }
        )
        
        // Use the loaded image
        displayImage(apngImage)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

## Platform Support

| Feature | Android | iOS | Desktop | Web |
|---------|---------|-----|---------|-----|
| APNG Parsing | ✅ | ✅ | ✅ | ✅ |
| File Loading | ✅ | ✅ | ✅ | ❌ |
| Network Loading | ✅ | ✅ | ✅ | ✅ |
| Resource Loading | ✅ | ✅ | ✅ | ❌ |
| Caching | ✅ | ✅ | ✅ | ⚠️ |
| HTTP Client | Ktor/OkHttp | Ktor/Darwin | Ktor/OkHttp | Ktor/JS |

## Build and Run

### Android

```shell
./gradlew :composeApp:assembleDebug
```

### Desktop

```shell
./gradlew :composeApp:run
```

### iOS

Open `iosApp` directory in Xcode and build from there.

### Web (WASM)

```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## Cache Strategy Interface

```kotlin
interface ApngCacheStrategy {
    fun path(url: String): Path?
    suspend fun save(url: String, bytes: ByteArray): Path?
    suspend fun load(url: String): ByteArray?
    suspend fun clear()
}
```

Implement this interface to create a custom cache strategy:

```kotlin
class NoCacheStrategy : ApngCacheStrategy {
    override fun path(url: String) = null
    override suspend fun save(url: String, bytes: ByteArray) = null
    override suspend fun load(url: String): ByteArray? = null
    override suspend fun clear() {}
}
```

## Load Result Types

```kotlin
sealed interface ApngCompositionResult {
    data class Loading(val progress: Float?) : ApngCompositionResult
    data class Success(val composition: ApngImage) : ApngCompositionResult
    data class Error(val exception: Exception) : ApngCompositionResult
}
```

## Performance Tips

1. **Use Caching**: Downloaded APNG files are automatically cached. Multiple accesses to the same URL won't trigger redundant downloads.

2. **Progress Feedback**: Use progress callbacks to improve user experience:
   ```kotlin
   rememberApngCompositionFromUrl(
       url = url,
       onProgress = { downloaded, total ->
           updateProgressBar(downloaded.toFloat() / total)
       }
   )
   ```

3. **Error Handling**: Provide clear error messages instead of silent failures.

4. **Custom Timeouts**: Adjust timeouts based on network conditions:
   ```kotlin
   DefaultHttpClient = KtorHttpClient(
       connectTimeoutMs = 60000,  // Increase for slow networks
       requestTimeoutMs = 120000
   )
   ```

## Frequently Asked Questions

**Q: Is there a file size limit for network loading?**

A: The default cache size is 100MB. Files larger than this won't be cached.

**Q: Does the library support proxies?**

A: Yes, you can configure a custom `HttpClient` implementation to support proxies.

**Q: Can Web platform load local files?**

A: No. Due to browser security restrictions, the Web platform only supports URL loading and byte arrays.

**Q: How do I disable caching?**

A: Implement the `ApngCacheStrategy` interface to return `null` for all cache operations.

## Migration Guide (from older versions)

### Old API

```kotlin
val state = rememberApngStateFromPath(path)
when (state) {
    is ApngLoadState.Success -> ApngImage(state.apngImage, ...)
}
```

### New API

```kotlin
val result = rememberApngCompositionSpec(
    ApngCompositionSpec.File(path)
)
when (result) {
    is ApngCompositionLoadResult.Success -> ApngImage(result.composition, ...)
}
```

## Architecture Highlights

### Modular Design

- Separation of concerns with dedicated modules
- Core library has no UI dependencies
- Network loading is optional and pluggable

### Unified Resource Loading

- Abstract `ApngSource` for all data sources
- Platform-specific implementations via `expect/actual`
- Consistent error handling across platforms

### Network Infrastructure

- Ktor-based HTTP client with automatic retry
- LRU disk cache with configurable size
- Progress callbacks for UI updates
- Extensible cache and HTTP client interfaces

### Cross-Platform Consistency

- Identical API across all platforms
- Platform-specific optimizations where needed
- Graceful degradation for unsupported features

## License

MIT License - see LICENSE file for details

## Contributing

Contributions are welcome! Please read our [contributing guidelines](docs/CONTRIBUTING.md) before submitting PRs.

## Acknowledgments

This library was optimized following the architectural patterns and best practices from the [Compottie](https://github.com/alexzhirkevich/compottie) project.

---

Learn more about:
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)
- [Kotlin/Wasm](https://kotl.in/wasm/)
