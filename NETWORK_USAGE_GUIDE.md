# APNG 网络加载使用指南

本文档说明如何使用新增的网络加载功能来加载和显示网络 APNG 文件。

## 基础使用

### 1. 从网络 URL 加载 APNG

#### 方式一：使用 Composable（推荐）

```kotlin
import io.github.lugf027.apng.compose.ApngImage
import io.github.lugf027.apng.compose.ApngCompositionSpec
import io.github.lugf027.apng.compose.rememberApngCompositionSpec
import io.github.lugf027.apng.compose.ApngCompositionLoadResult

@Composable
fun MyScreen() {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.Url(
            url = "https://example.com/animation.apng"
        )
    )
    
    when (result) {
        is ApngCompositionLoadResult.Loading -> {
            CircularProgressIndicator()
        }
        is ApngCompositionLoadResult.Success -> {
            ApngImage(
                composition = result.composition,
                contentDescription = "Animated icon",
                modifier = Modifier.size(100.dp)
            )
        }
        is ApngCompositionLoadResult.Error -> {
            Text("Failed to load: ${result.exception.message}")
        }
    }
}
```

#### 方式二：使用便利 Composable

```kotlin
import io.github.lugf027.apng.network.rememberApngCompositionFromUrl

@Composable
fun MyScreen() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    when (result) {
        is ApngCompositionResult.Loading -> CircularProgressIndicator()
        is ApngCompositionResult.Success -> ApngImage(result.composition, contentDescription = null)
        is ApngCompositionResult.Error -> Text("Error: ${result.exception.message}")
    }
}
```

### 2. 从本地文件加载

```kotlin
import io.github.lugf027.apng.compose.ApngCompositionSpec

@Composable
fun MyScreen() {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.File(
            path = "/sdcard/Download/animation.apng"  // Android
            // path = "/Documents/animation.apng"      // Desktop
        )
    )
    
    when (result) {
        is ApngCompositionLoadResult.Success -> ApngImage(result.composition, null)
        is ApngCompositionLoadResult.Error -> Text("Failed to load")
        else -> CircularProgressIndicator()
    }
}
```

### 3. 从 Compose Resources 加载

```kotlin
import io.github.lugf027.apng.compose.ApngCompositionSpec

@Composable
fun MyScreen() {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.Resource(
            resourcePath = "drawable/animation.apng"
        )
    )
    
    when (result) {
        is ApngCompositionLoadResult.Success -> ApngImage(result.composition, null)
        is ApngCompositionLoadResult.Error -> Text("Failed to load")
        else -> CircularProgressIndicator()
    }
}
```

### 4. 从字节数组加载

```kotlin
import io.github.lugf027.apng.compose.ApngCompositionSpec

@Composable
fun MyScreen(data: ByteArray) {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.Bytes(data = data)
    )
    
    when (result) {
        is ApngCompositionLoadResult.Success -> ApngImage(result.composition, null)
        is ApngCompositionLoadResult.Error -> Text("Failed to load")
        else -> CircularProgressIndicator()
    }
}
```

## 高级功能

### 进度监听

```kotlin
import io.github.lugf027.apng.network.rememberApngCompositionFromUrl
import androidx.compose.material3.LinearProgressIndicator

@Composable
fun MyScreen() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    when (result) {
        is ApngCompositionResult.Loading -> {
            // 显示进度
            val progress = result.progress ?: 0f
            Column {
                LinearProgressIndicator(progress)
                Text("Downloading: ${(progress * 100).toInt()}%")
            }
        }
        is ApngCompositionResult.Success -> ApngImage(result.composition, null)
        is ApngCompositionResult.Error -> Text("Failed: ${result.exception.message}")
    }
}
```

### 自定义缓存策略

```kotlin
import io.github.lugf027.apng.network.DiskApngCacheStrategy
import io.github.lugf027.apng.network.rememberApngCompositionFromUrl

@Composable
fun MyScreen() {
    // 使用默认缓存
    val result1 = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    // 使用自定义缓存大小
    val customCache = DiskApngCacheStrategy.Instance  // 默认 100MB
    val result2 = rememberApngCompositionFromUrl(
        url = "https://example.com/animation2.apng",
        cacheStrategy = customCache
    )
}
```

### 自定义 HTTP 客户端

```kotlin
import io.github.lugf027.apng.network.DefaultHttpClient
import io.github.lugf027.apng.network.KtorHttpClient

// 在应用启动时配置
fun initializeNetwork() {
    // 使用 Ktor 客户端（默认）
    DefaultHttpClient = KtorHttpClient(
        maxRetries = 5,
        connectTimeoutMs = 30000,
        requestTimeoutMs = 60000
    )
}
```

### 命令式加载（协程）

```kotlin
import io.github.lugf027.apng.core.ApngLoader
import io.github.lugf027.apng.network.loadFromUrl

// 在协程中使用
lifecycleScope.launch {
    try {
        val apngImage = ApngLoader().loadFromUrl(
            url = "https://example.com/animation.apng",
            onProgress = { downloaded, total ->
                println("Downloaded: $downloaded / $total bytes")
            }
        )
        displayImage(apngImage)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

## 缓存管理

### 查看缓存

```kotlin
import io.github.lugf027.apng.network.DiskApngCacheStrategy

val cache = DiskApngCacheStrategy.Instance
val cached = cache.path("https://example.com/animation.apng")
if (cached != null) {
    println("Found in cache: $cached")
}
```

### 清空缓存

```kotlin
import io.github.lugf027.apng.network.DiskApngCacheStrategy

lifecycleScope.launch {
    val cache = DiskApngCacheStrategy.Instance
    cache.clear()
    println("Cache cleared")
}
```

## 各平台特性

### Android
- ✅ 支持所有加载方式（URL、文件、资源、字节）
- ✅ 使用 Ktor OkHttp 引擎
- ✅ 支持后台线程下载

### iOS
- ✅ 支持所有加载方式（URL、文件、资源、字节）
- ✅ 使用 Ktor Darwin 引擎（NSURLSession）
- ✅ 自动处理 SSL 验证

### Desktop
- ✅ 支持所有加载方式（URL、文件、资源、字节）
- ✅ 使用 Ktor OkHttp 引擎
- ✅ 支持代理配置

### Web
- ⚠️ 仅支持 URL 和字节加载
- ⚠️ URL 源受 CORS 限制
- ✅ 使用 Ktor JS 引擎（Fetch API）

## 错误处理

```kotlin
@Composable
fun MyScreen() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    when (result) {
        is ApngCompositionResult.Error -> {
            val exception = result.exception
            when {
                exception is HttpException && exception.statusCode == 404 -> {
                    Text("File not found on server")
                }
                exception is HttpException -> {
                    Text("HTTP Error: ${exception.statusCode}")
                }
                exception is java.io.IOException -> {
                    Text("Network error. Check your connection.")
                }
                else -> {
                    Text("Unexpected error: ${exception.message}")
                }
            }
        }
        else -> {}
    }
}
```

## 最佳实践

1. **在应用启动时初始化网络**
   ```kotlin
   class MyApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           initializeApngNetwork()  // 初始化 Ktor 客户端
       }
   }
   ```

2. **合理使用缓存**
   - 对于频繁访问的资源，缓存可以显著提高性能
   - 定期清理缓存以释放空间

3. **提供加载状态反馈**
   - 显示加载进度指示器
   - 提供清晰的错误信息

4. **处理网络异常**
   - 捕获 `IOException` 进行重试
   - 显示有意义的错误提示

## 迁移指南

如果使用旧 API：

```kotlin
// 旧的 API
val state = rememberApngStateFromPath(path)
ApngImage(state, ...)

// 新的 API
val result = rememberApngCompositionSpec(
    ApngCompositionSpec.File(path)
)
```

新 API 提供以下优势：
- 统一的多源支持
- 更好的错误处理
- 网络加载能力
- 内置缓存支持
