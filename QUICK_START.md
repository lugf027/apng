# APNG 库快速开始指南

## 概述

这是一个优化后的 Kotlin Multiplatform APNG 库，具有高级的模块化架构、统一的资源加载接口和完整的网络支持。

## 新增功能（v2.0）

✨ **架构优化**
- 模块化设计，核心库无 UI 依赖
- 统一的多源资源加载接口
- 灵活的缓存策略

✨ **网络加载**
- 从 URL 下载 APNG 文件
- 自动 LRU 磁盘缓存
- 进度回调支持
- 自动重试（指数退避）

✨ **资源加载**
- 支持本地文件
- 支持 Compose Resources
- 支持字节数组
- 支持网络 URL

## 项目结构

```
apng/
├── apng-core/              # 核心 APNG 解析库（无 UI）
├── apng-network-core/      # 网络加载基础设施
│   ├── ApngSource          # 数据源抽象
│   ├── ApngResourceLoader  # 资源加载接口
│   ├── ApngCacheStrategy   # 缓存策略接口
│   ├── DiskLruCache        # LRU 缓存实现
│   └── HttpClient          # HTTP 客户端接口
├── apng-network/           # 网络加载实现
│   ├── KtorHttpClient      # Ktor 集成
│   └── 网络 Composables
├── apng-resources/         # 资源加载支持
│   └── 资源 Composables
└── apng-compose/           # Compose UI 组件
    ├── ApngImage           # 主组件
    ├── ApngPainter         # 绘制器
    └── ApngCompositionSpec # 多源加载
```

## 快速开始

### 1. 基础用法 - 从字节数组加载

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

### 2. 从网络 URL 加载（新功能！）

```kotlin
import io.github.lugf027.apng.network.rememberApngCompositionFromUrl

@Composable
fun DisplayApngFromUrl() {
    val result = rememberApngCompositionFromUrl(
        url = "https://example.com/animation.apng"
    )
    
    when (result) {
        is ApngCompositionResult.Loading -> {
            // 显示进度
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

### 3. 从本地文件加载

```kotlin
import io.github.lugf027.apng.compose.ApngCompositionSpec

@Composable
fun DisplayApngFromFile(filePath: String) {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.File(filePath)
    )
    // 处理 result...
}
```

### 4. 从 Compose Resources 加载

```kotlin
import io.github.lugf027.apng.compose.ApngCompositionSpec

@Composable
fun DisplayApngFromResources() {
    val result = rememberApngCompositionSpec(
        spec = ApngCompositionSpec.Resource("drawable/animation.apng")
    )
    // 处理 result...
}
```

## 网络加载配置

### 应用启动时初始化

```kotlin
import io.github.lugf027.apng.network.initializeApngNetwork
import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化网络模块
        initializeApngNetwork()
    }
}
```

### 自定义配置

```kotlin
import io.github.lugf027.apng.network.KtorHttpClient
import io.github.lugf027.apng.network.DiskApngCacheStrategy
import io.github.lugf027.apng.network.DefaultHttpClient
import io.github.lugf027.apng.network.DefaultCacheStrategy

fun setupNetwork() {
    // 自定义 HTTP 客户端
    DefaultHttpClient = KtorHttpClient(
        maxRetries = 5,              // 最大重试次数
        connectTimeoutMs = 30000,    // 连接超时
        requestTimeoutMs = 60000     // 请求超时
    )
    
    // 使用默认缓存（可自定义）
    // DefaultCacheStrategy = DiskApngCacheStrategy.Instance
}
```

## 缓存管理

```kotlin
import io.github.lugf027.apng.network.DiskApngCacheStrategy
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

val cache = DiskApngCacheStrategy.Instance

// 查询缓存
val cachedPath = cache.path("https://example.com/animation.apng")
if (cachedPath != null) {
    println("Found in cache: $cachedPath")
}

// 清空所有缓存
lifecycleScope.launch {
    cache.clear()
    println("Cache cleared")
}
```

## 错误处理

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

## 构建和运行

### Android
```bash
./gradlew :composeApp:assembleDebug
```

### Desktop
```bash
./gradlew :composeApp:run
```

### iOS
```bash
# 使用 Xcode 打开 iosApp 文件夹
open iosApp
```

### Web (WASM)
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## 核心 API

### 数据源

```kotlin
// 字节数组
ApngSource.Bytes(data)

// 本地文件
ApngSource.File(path)

// 网络 URL
ApngSource.Url(url)

// Compose Resources
ApngSource.Resource(resourcePath)
```

### 加载结果

```kotlin
sealed interface ApngCompositionResult {
    data class Loading(val progress: Float?) : ApngCompositionResult
    data class Success(val composition: ApngImage) : ApngCompositionResult
    data class Error(val exception: Exception) : ApngCompositionResult
}
```

### 缓存策略

```kotlin
interface ApngCacheStrategy {
    fun path(url: String): Path?
    suspend fun save(url: String, bytes: ByteArray): Path?
    suspend fun load(url: String): ByteArray?
    suspend fun clear()
}
```

## 各平台特性

| 功能 | Android | iOS | Desktop | Web |
|------|---------|-----|---------|-----|
| APNG 解析 | ✅ | ✅ | ✅ | ✅ |
| 网络加载 | ✅ | ✅ | ✅ | ✅ |
| 文件加载 | ✅ | ✅ | ✅ | ❌ |
| 资源加载 | ✅ | ✅ | ✅ | ❌ |
| 缓存支持 | ✅ | ✅ | ✅ | ⚠️ |
| HTTP 客户端 | Ktor/OkHttp | Ktor/Darwin | Ktor/OkHttp | Ktor/JS |

## 命令式 API（协程）

```kotlin
import io.github.lugf027.apng.core.ApngLoader

lifecycleScope.launch {
    try {
        // 直接从 URL 加载
        val apngImage = ApngLoader().loadFromUrl(
            url = "https://example.com/animation.apng",
            onProgress = { downloaded, total ->
                println("Progress: $downloaded / $total bytes")
            }
        )
        
        // 使用加载的图像
        displayImage(apngImage)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

## 迁移指南（从旧版本）

### 旧 API
```kotlin
val state = rememberApngStateFromPath(path)
when (state) {
    is ApngLoadState.Success -> ApngImage(state.apngImage, ...)
}
```

### 新 API
```kotlin
val result = rememberApngCompositionSpec(
    ApngCompositionSpec.File(path)
)
when (result) {
    is ApngCompositionLoadResult.Success -> ApngImage(result.composition, ...)
}
```

## 性能提示

1. **使用缓存**：网络加载的 APNG 会自动缓存，多次访问同一 URL 不会重复下载

2. **进度反馈**：使用进度回调提升用户体验
   ```kotlin
   rememberApngCompositionFromUrl(
       url = url,
       onProgress = { downloaded, total ->
           updateProgressBar(downloaded.toFloat() / total)
       }
   )
   ```

3. **错误处理**：提供清晰的错误提示而不是默认处理

4. **自定义超时**：根据网络情况调整超时时间
   ```kotlin
   DefaultHttpClient = KtorHttpClient(
       connectTimeoutMs = 60000,  // 对于慢速网络增加超时
       requestTimeoutMs = 120000
   )
   ```

## 常见问题

**Q: 网络加载有文件大小限制吗？**
A: 默认缓存大小为 100MB。大于此大小的文件不会被缓存。

**Q: 是否支持代理？**
A: 可以通过配置自定义 `HttpClient` 实现代理支持。

**Q: Web 平台可以加载本地文件吗？**
A: 不支持。Web 平台由于安全限制，仅支持通过 URL 加载和字节数组。

**Q: 如何禁用缓存？**
A: 实现 `ApngCacheStrategy` 接口返回 `null`：
```kotlin
class NoCacheStrategy : ApngCacheStrategy {
    override fun path(url: String) = null
    override suspend fun save(url: String, bytes: ByteArray) = null
    override suspend fun load(url: String): ByteArray? = null
    override suspend fun clear() {}
}
```

## 更多文档

- [完整 README](./README.md)
- [网络使用指南](./NETWORK_USAGE_GUIDE.md)
- [实现细节](./IMPLEMENTATION.md)

## 下一步

- 集成到你的项目中
- 根据需求自定义缓存和 HTTP 客户端
- 提供 APNG 文件进行测试
- 参与贡献！

---

**项目路径**: `/Users/donaldlu/Documents/workspace/android/apng`

**参考**:  基于 [Compottie](https://github.com/alexzhirkevich/compottie) 的架构最佳实践优化
