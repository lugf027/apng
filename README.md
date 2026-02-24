# APNG Kotlin Multiplatform

[中文版本](./README_zh.md)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg)](https://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-blue.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lugf027/apng-core.svg)](https://central.sonatype.com/search?q=io.github.lugf027)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A high-performance [APNG (Animated PNG)](https://wiki.mozilla.org/APNG_Specification) parsing and rendering library developed based on Kotlin Multiplatform (KMP). It supports consistent rendering effects on Android, iOS, Desktop (JVM), and Web (Wasm/JS) platforms.

| Android | Desktop (JVM) | iOS | macOS | JS | WasmJS |
|:-------:|:-------------:|:---:|:-----:|:--:|:------:|
| ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

<p align="center">
  <img src="docs/images/demo_desktop.png" alt="Desktop Demo" width="600"/>
</p>

## Setup

Add the dependencies you need to your `build.gradle.kts`:

```kotlin
// Core: APNG parsing + frame composing + Compose animation rendering (required)
implementation("io.github.lugf027:apng-core:<version>")

// Network: Ktor-based URL loading with disk LRU caching
implementation("io.github.lugf027:apng-network:<version>")

// Resources: Load APNG from KMP Resources (Res)
implementation("io.github.lugf027:apng-resources:<version>")
```

> `apng-network` transitively includes `apng-network-core` (disk cache layer). You only need `apng-network-core` directly if you want caching without the built-in Ktor client.

## Usage

### 1. Load an APNG Composition

```kotlin
// From byte array
val composition by rememberApngComposition(
    ApngCompositionSpec.Bytes(byteArray, cacheKey = "my_anim")
)

// From URL (requires apng-network)
val composition by rememberApngComposition(
    ApngCompositionSpec.Url("https://example.com/animation.apng")
)

// From Compose Resources (requires apng-resources)
val composition by rememberApngComposition(
    ApngCompositionSpec.ComposeResource { Res.readBytes("files/animation.apng") }
)
```

### 2. Render the Animation

**Declarative (recommended):**

```kotlin
val progress by animateApngCompositionAsState(
    composition,
    isPlaying = true,
    iterations = Apng.IterateForever,
)
val painter = rememberApngPainter(composition, progress = { progress })
Image(painter = painter, contentDescription = null)
```

**Imperative:**

```kotlin
val animatable = rememberApngAnimatable()
LaunchedEffect(composition) {
    animatable.animate(composition, iterations = Apng.IterateForever)
}
val painter = rememberApngPainter(composition, progress = { animatable.progress })
Image(painter = painter, contentDescription = null)
```

**Shorthand:**

```kotlin
val painter = rememberApngPainter(composition, isPlaying = true)
Image(painter = painter, contentDescription = null)
```

### 3. Playback Control

```kotlin
val progress by animateApngCompositionAsState(
    composition,
    isPlaying = true,
    speed = 1.5f,                          // Playback speed
    iterations = Apng.IterateForever,      // Loop forever (or pass an Int)
    clipSpec = ApngClipSpec.Frame(2, 8),   // Play only frames 2–8
)
```

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| **apng-core** | `io.github.lugf027:apng-core` | APNG parsing, frame composing, and Compose animation rendering |
| **apng-network-core** | `io.github.lugf027:apng-network-core` | Disk LRU cache and network caching strategy |
| **apng-network** | `io.github.lugf027:apng-network` | Ktor-based network loading with built-in disk caching |
| **apng-resources** | `io.github.lugf027:apng-resources` | KMP Resources integration |

### Module Dependencies

```
apng-core                  ← standalone
apng-network-core          ← apng-core + okio
apng-network               ← apng-core + apng-network-core + ktor-client-core
apng-resources             ← apng-core + compose.components.resources
```

## Key APIs

| Class / Function | Purpose |
|-----------------|---------|
| `ApngComposition` | Pre-composed frame data container |
| `ApngCompositionSpec` | Loading specification (Bytes / Url / ComposeResource) |
| `rememberApngComposition()` | Composable that loads and caches a composition |
| `animateApngCompositionAsState()` | Declarative animation driver |
| `rememberApngAnimatable()` | Imperative animation controller |
| `rememberApngPainter()` | Creates a `Painter` from a composition + progress |
| `ApngClipSpec` | Frame/progress range clipping |
| `Apng.IterateForever` | Constant for infinite looping |

## Platform Details

| Feature | Android | Skiko (JVM / iOS / macOS / JS / WasmJS) |
|---------|---------|------------------------------------------|
| Image decoding | `BitmapFactory` | `Image.makeFromEncoded` (Skia) |
| Frame composing | Android Canvas + PorterDuff | Skia Canvas + BlendMode |
| Disk cache | ✅ (`FileSystem.SYSTEM`) | JVM/Native ✅ · Web ❌ |

## Running the Example

```bash
# Desktop
./gradlew :example:desktopApp:run

# Android
./gradlew :example:androidApp:installDebug

# Web (JS)
./gradlew :example:webApp:jsBrowserDevelopmentRun

# Web (WasmJS)
./gradlew :example:webApp:wasmJsBrowserDevelopmentRun

# iOS — open in Xcode
open example/iosApp/iosApp.xcodeproj
```

## License

```
MIT License

Copyright (c) 2026 lugf027
```

See [LICENSE](LICENSE) for details.
