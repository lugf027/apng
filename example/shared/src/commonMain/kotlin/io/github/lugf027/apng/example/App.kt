package io.github.lugf027.apng.example

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lugf027.apng.*
import io.github.lugf027.shared.generated.resources.Res

// ── Data ──

private data class ApngAsset(
    val fileName: String,
    val displayName: String,
    val cacheKey: String = fileName,
)

private val LOCAL_ASSETS = listOf(
    ApngAsset("elephant_apng.apng", "Elephant"),
    ApngAsset("spinfox.apng", "Spin Fox"),
    ApngAsset("APNG-cube.apng", "Cube"),
    ApngAsset("APNG-4D.apng", "4D Shape"),
    ApngAsset("pyani.apng", "Pyani"),
    ApngAsset("over_none.apng", "Dispose: None"),
    ApngAsset("over_background.apng", "Dispose: BG"),
    ApngAsset("over_previous.apng", "Dispose: Prev"),
)

private const val NETWORK_APNG_URL =
    "https://upload.wikimedia.org/wikipedia/commons/1/14/Animated_PNG_example_bouncing_beach_ball.png"

// ── Navigation ──

private sealed class Screen {
    data object Gallery : Screen()
    data class Detail(val asset: ApngAsset) : Screen()
    data object Network : Screen()
}

// ── App Entry ──

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        var screen by remember { mutableStateOf<Screen>(Screen.Gallery) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    if (targetState is Screen.Gallery) {
                        (fadeIn() + slideInHorizontally { -it / 4 })
                            .togetherWith(fadeOut() + slideOutHorizontally { it / 4 })
                    } else {
                        (fadeIn() + slideInHorizontally { it / 4 })
                            .togetherWith(fadeOut() + slideOutHorizontally { -it / 4 })
                    }
                },
                contentKey = { it::class },
            ) { currentScreen ->
                when (currentScreen) {
                    is Screen.Gallery -> GalleryScreen(
                        onAssetClick = { screen = Screen.Detail(it) },
                        onNetworkClick = { screen = Screen.Network },
                    )
                    is Screen.Detail -> DetailScreen(
                        asset = currentScreen.asset,
                        onBack = { screen = Screen.Gallery },
                    )
                    is Screen.Network -> NetworkDetailScreen(
                        onBack = { screen = Screen.Gallery },
                    )
                }
            }
        }
    }
}

// ── Responsive helpers ──

@Composable
private fun isCompactWidth(): Boolean {
    val width = LocalWindowWidth.current
    return width < 600
}

// Simple window width provider using BoxWithConstraints
private val LocalWindowWidth = staticCompositionLocalOf { 400 }

@Composable
private fun WithWindowWidth(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalWindowWidth provides maxWidth.value.toInt()
        ) {
            content()
        }
    }
}

// ── Gallery Screen ──

@Composable
private fun GalleryScreen(
    onAssetClick: (ApngAsset) -> Unit,
    onNetworkClick: () -> Unit,
) {
    WithWindowWidth {
        val compact = isCompactWidth()
        val columns = when {
            LocalWindowWidth.current >= 1200 -> 4
            LocalWindowWidth.current >= 800 -> 3
            LocalWindowWidth.current >= 600 -> 3
            else -> 2
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopBar(title = "APNG Gallery")

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = if (compact) 12.dp else 24.dp,
                    vertical = 12.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
            ) {
                // Network card
                item {
                    NetworkGalleryCard(onClick = onNetworkClick)
                }

                // Local assets
                items(LOCAL_ASSETS) { asset ->
                    AssetGalleryCard(asset = asset, onClick = { onAssetClick(asset) })
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onBack: (() -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp),
                ) {
                    Text("←", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ── Gallery Cards ──

@Composable
private fun AssetGalleryCard(
    asset: ApngAsset,
    onClick: () -> Unit,
) {
    val composition by rememberApngComposition {
        ApngCompositionSpec.ComposeResource(
            cacheKey = asset.cacheKey,
            readBytes = { Res.readBytes("files/${asset.fileName}") },
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Animation preview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF12121F)),
                contentAlignment = Alignment.Center,
            ) {
                val comp = composition
                if (comp == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    val painter = rememberApngPainter(
                        composition = comp,
                        isPlaying = true,
                        iterations = Apng.IterateForever,
                    )
                    Image(
                        painter = painter,
                        contentDescription = asset.displayName,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text = asset.displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Info badge
            composition?.let { comp ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${comp.frameCount}f · ${formatDuration(comp)} · ${comp.width}×${comp.height}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun NetworkGalleryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B2838),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🌐", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Network APNG",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF81D4FA),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Load from URL",
                fontSize = 10.sp,
                color = Color(0xFF81D4FA).copy(alpha = 0.6f),
            )
        }
    }
}

// ── Detail Screen ──

@Composable
private fun DetailScreen(
    asset: ApngAsset,
    onBack: () -> Unit,
) {
    val composition by rememberApngComposition {
        ApngCompositionSpec.ComposeResource(
            cacheKey = asset.cacheKey,
            readBytes = { Res.readBytes("files/${asset.fileName}") },
        )
    }

    var isPlaying by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(1f) }
    var iterations by remember { mutableStateOf(Apng.IterateForever) }
    var scale by remember { mutableStateOf(1f) }

    WithWindowWidth {
        val compact = isCompactWidth()

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = asset.displayName, onBack = onBack)

            if (compact) {
                // Mobile / narrow: vertical layout
                CompactDetailLayout(
                    composition = composition,
                    isPlaying = isPlaying,
                    speed = speed,
                    iterations = iterations,
                    scale = scale,
                    onPlayingChange = { isPlaying = it },
                    onSpeedChange = { speed = it },
                    onIterationsChange = { iterations = it },
                    onScaleChange = { scale = it },
                )
            } else {
                // Desktop / wide: side-by-side layout
                WideDetailLayout(
                    composition = composition,
                    isPlaying = isPlaying,
                    speed = speed,
                    iterations = iterations,
                    scale = scale,
                    onPlayingChange = { isPlaying = it },
                    onSpeedChange = { speed = it },
                    onIterationsChange = { iterations = it },
                    onScaleChange = { scale = it },
                )
            }
        }
    }
}

@Composable
private fun CompactDetailLayout(
    composition: ApngComposition?,
    isPlaying: Boolean,
    speed: Float,
    iterations: Int,
    scale: Float,
    onPlayingChange: (Boolean) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onIterationsChange: (Int) -> Unit,
    onScaleChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnimationPreview(
            composition = composition,
            isPlaying = isPlaying,
            speed = speed,
            iterations = iterations,
            scale = scale,
            previewSize = 260.dp,
        )
        InfoCard(composition)
        ControlsCard(
            isPlaying = isPlaying,
            speed = speed,
            iterations = iterations,
            scale = scale,
            onPlayingChange = onPlayingChange,
            onSpeedChange = onSpeedChange,
            onIterationsChange = onIterationsChange,
            onScaleChange = onScaleChange,
        )
    }
}

@Composable
private fun WideDetailLayout(
    composition: ApngComposition?,
    isPlaying: Boolean,
    speed: Float,
    iterations: Int,
    scale: Float,
    onPlayingChange: (Boolean) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onIterationsChange: (Int) -> Unit,
    onScaleChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Left: preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            AnimationPreview(
                composition = composition,
                isPlaying = isPlaying,
                speed = speed,
                iterations = iterations,
                scale = scale,
                previewSize = 360.dp,
            )
        }

        // Right: info + controls
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            InfoCard(composition)
            ControlsCard(
                isPlaying = isPlaying,
                speed = speed,
                iterations = iterations,
                scale = scale,
                onPlayingChange = onPlayingChange,
                onSpeedChange = onSpeedChange,
                onIterationsChange = onIterationsChange,
                onScaleChange = onScaleChange,
            )
        }
    }
}

// ── Shared Components ──

@Composable
private fun AnimationPreview(
    composition: ApngComposition?,
    isPlaying: Boolean,
    speed: Float,
    iterations: Int,
    scale: Float,
    previewSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(previewSize)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D0D1A)),
        contentAlignment = Alignment.Center,
    ) {
        val comp = composition
        if (comp == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            val painter = rememberApngPainter(
                composition = comp,
                isPlaying = isPlaying,
                iterations = iterations,
                speed = speed,
            )
            Image(
                painter = painter,
                contentDescription = "APNG",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun InfoCard(composition: ApngComposition?) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Animation Info",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (composition == null) {
                Text(
                    "Loading...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            } else {
                InfoRow("Frames", "${composition.frameCount}")
                InfoRow("Duration", formatDuration(composition))
                InfoRow("Size", "${composition.width} × ${composition.height} px")
                InfoRow("Avg Frame", "${formatMs(composition.duration.inWholeMilliseconds.toFloat() / composition.frameCount.coerceAtLeast(1))} ms")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ControlsCard(
    isPlaying: Boolean,
    speed: Float,
    iterations: Int,
    scale: Float,
    onPlayingChange: (Boolean) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onIterationsChange: (Int) -> Unit,
    onScaleChange: (Float) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Controls",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Play / Pause + Loop
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilledTonalButton(
                    onClick = { onPlayingChange(!isPlaying) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(if (isPlaying) "⏸ Pause" else "▶ Play", fontSize = 13.sp)
                }
                FilledTonalButton(
                    onClick = {
                        onIterationsChange(
                            if (iterations == Apng.IterateForever) 1 else Apng.IterateForever
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        if (iterations == Apng.IterateForever) "Loop: ∞" else "Loop: 1×",
                        fontSize = 13.sp,
                    )
                }
            }

            // Speed
            SliderControl(
                label = "Speed",
                value = speed,
                valueRange = 0.1f..3f,
                displayText = "${formatMs(speed * 10f).toFloat().let { (it / 10f).toString().take(4) }}x",
                onValueChange = onSpeedChange,
            )

            // Scale
            SliderControl(
                label = "Scale",
                value = scale,
                valueRange = 0.25f..3f,
                displayText = "${(scale * 100).toInt()}%",
                onValueChange = onScaleChange,
            )
        }
    }
}

@Composable
private fun SliderControl(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayText: String,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Text(
                displayText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Network Detail Screen ──

@Composable
private fun NetworkDetailScreen(onBack: () -> Unit) {
    val composition by rememberApngComposition {
        ApngCompositionSpec.Url(NETWORK_APNG_URL)
    }

    var isPlaying by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(1f) }
    var iterations by remember { mutableStateOf(Apng.IterateForever) }
    var scale by remember { mutableStateOf(1f) }

    WithWindowWidth {
        val compact = isCompactWidth()

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = "Network APNG", onBack = onBack)

            if (compact) {
                CompactDetailLayout(
                    composition = composition,
                    isPlaying = isPlaying,
                    speed = speed,
                    iterations = iterations,
                    scale = scale,
                    onPlayingChange = { isPlaying = it },
                    onSpeedChange = { speed = it },
                    onIterationsChange = { iterations = it },
                    onScaleChange = { scale = it },
                )
            } else {
                WideDetailLayout(
                    composition = composition,
                    isPlaying = isPlaying,
                    speed = speed,
                    iterations = iterations,
                    scale = scale,
                    onPlayingChange = { isPlaying = it },
                    onSpeedChange = { speed = it },
                    onIterationsChange = { iterations = it },
                    onScaleChange = { scale = it },
                )
            }
        }
    }
}

// ── Utilities ──

private fun formatDuration(composition: ApngComposition): String {
    val ms = composition.duration.inWholeMilliseconds
    return if (ms >= 1000) {
        "${ms / 1000f}s"
    } else {
        "${ms}ms"
    }
}

private fun formatMs(value: Float): String {
    return if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        ((value * 10).toInt() / 10f).toString()
    }
}
