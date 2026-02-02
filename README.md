# APNG Kotlin Multiplatform Library

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

### Basic Usage

```kotlin
import io.github.lugf027.apng.compose.ApngImage
import io.github.lugf027.apng.compose.ApngCompositionSpec
import io.github.lugf027.apng.compose.rememberApngCompositionSpec

@Composable
fun MyScreen() {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.Url("https://example.com/animation.apng")
    )
    
    when (result) {
        is ApngCompositionLoadResult.Loading -> CircularProgressIndicator()
        is ApngCompositionLoadResult.Success -> ApngImage(result.composition, "Animation")
        is ApngCompositionLoadResult.Error -> Text("Failed to load")
    }
}
```

### Different Data Sources

```kotlin
// From URL (with network loading)
ApngCompositionSpec.Url("https://example.com/animation.apng")

// From local file
ApngCompositionSpec.File("/path/to/animation.apng")

// From Compose Resources
ApngCompositionSpec.Resource("drawable/animation.apng")

// From byte array
ApngCompositionSpec.Bytes(byteArray)
```

## Installation

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

## Network Loading

### With Default Settings

```kotlin
@Composable
fun LoadFromUrl() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    // ... handle result
}
```

### With Progress Tracking

```kotlin
import io.github.lugf027.apng.network.rememberApngCompositionFromUrl

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

### Custom Configuration

```kotlin
import io.github.lugf027.apng.network.KtorHttpClient
import io.github.lugf027.apng.network.DiskApngCacheStrategy
import io.github.lugf027.apng.network.DefaultHttpClient
import io.github.lugf027.apng.network.DefaultCacheStrategy

// Configure at app startup
fun initializeNetwork() {
    DefaultHttpClient = KtorHttpClient(
        maxRetries = 5,
        connectTimeoutMs = 30000,
        requestTimeoutMs = 60000
    )
    
    DefaultCacheStrategy = DiskApngCacheStrategy.Instance
}
```

## Caching

The library automatically caches downloaded APNG files using LRU (Least Recently Used) strategy:

```kotlin
import io.github.lugf027.apng.network.DiskApngCacheStrategy

val cache = DiskApngCacheStrategy.Instance

// Check if URL is cached
val cachedPath = cache.path("https://example.com/animation.apng")

// Clear all cache
lifecycleScope.launch {
    cache.clear()
}
```

Default cache location:
- Android: `context.cacheDir/apng-cache`
- iOS: App Documents directory
- Desktop: System temp directory
- Web: Browser IndexedDB (not persisted)

Default cache size: **100 MB**

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

## Documentation

For detailed usage examples and API reference, see:
- [Network Usage Guide](./NETWORK_USAGE_GUIDE.md)
- [Implementation Details](./IMPLEMENTATION.md)
- [Quick Start](./QUICK_START.md)

## Architecture Improvements

This version includes significant architectural improvements over the initial implementation:

### 1. Modular Design
- Separation of concerns with dedicated modules
- Core library has no UI dependencies
- Network loading is optional and pluggable

### 2. Unified Resource Loading
- Abstract `ApngSource` for all data sources
- Platform-specific implementations via `expect/actual`
- Consistent error handling across platforms

### 3. Network Loading Infrastructure
- Ktor-based HTTP client with automatic retry
- LRU disk cache with configurable size
- Progress callbacks for UI updates
- Extensible cache and HTTP client interfaces

### 4. Cross-Platform Consistency
- Identical API across all platforms
- Platform-specific optimizations where needed
- Graceful degradation for unsupported features

## License

MIT License - see LICENSE file for details

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

## Acknowledgments

This library was optimized following the architectural patterns and best practices from the [Compottie](https://github.com/alexzhirkevich/compottie) project.

---

Learn more about:
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)
- [Kotlin/Wasm](https://kotl.in/wasm/)
