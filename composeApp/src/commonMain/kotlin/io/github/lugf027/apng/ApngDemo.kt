package io.github.lugf027.apng

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import apng.composeapp.generated.resources.Res
import io.github.lugf027.apng.compose.ApngImage
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * APNG 资源项数据类
 */
data class ApngResource(
    val name: String,
    val fileName: String,
    val description: String
)

/**
 * APNG Demo 资源列表
 */
val apngResources = listOf(
    ApngResource("3D Cube", "APNG-cube.apng", "Rotating 3D cube anim"),
    ApngResource("Ball", "ball.apng", "Bouncing ball animation"),
    ApngResource("Elephant", "elephant_apng.apng", "Elephant animation"),
    ApngResource("Maneki Neko", "maneki-neko.apng", "Lucky cat animation"),
    ApngResource("Spin Fox", "spinfox.apng", "Spinning fox animation"),
    ApngResource("Pyani", "pyani.apng", "Pyani animation"),
    ApngResource("Over Background", "over_background.apng", "Background overlay test"),
    ApngResource("Over None", "over_none.apng", "No overlay test"),
    ApngResource("Over Previous", "over_previous.apng", "Previous frame overlay test"),
    ApngResource("4D", "APNG-4D.apng", "4D animation effect"),
    ApngResource("Minimal", "minimal.apng", "Minimal APNG example"),
    ApngResource("Alpha", "tRNS_alpha.apng", "Alpha channel test"),
)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ApngDemoScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "APNG Demo",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "Kotlin Multiplatform APNG Parse And Render",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Tab Selection
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Resources") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Network") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Info") }
                    )
                }

                // Tab Content
                when (selectedTab) {
                    0 -> ResourcesTab()
                    1 -> NetworkTab()
                    2 -> InfoTab()
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ResourcesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Bundled Resources",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // APNG Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(apngResources) { resource ->
                ApngCard(resource)
            }
        }
    }
}

@Composable
private fun NetworkTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Network Loading",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Network load example info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Platform Support",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                listOf(
                    "Android" to "✅ Full support",
                    "iOS" to "✅ Full support (Darwin HTTP)",
                    "Desktop" to "✅ Full support",
                    "Web" to "✅ Fetch API support"
                ).forEach { (platform, status) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            platform,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            status,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Features
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Features",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                listOf(
                    "🌐 HTTP/HTTPS URL loading",
                    "💾 LRU disk caching (iOS/Desktop/Android)",
                    "⏱️ Automatic retry with exponential backoff",
                    "📊 Progress tracking support",
                    "🔒 SHA256 cache key encryption",
                    "⚡ Memory caching on Web platform"
                ).forEach { feature ->
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Project Information",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                InfoRow("Library", "APNG Parser & Renderer")
                InfoRow("Version", "2.0")
                InfoRow("Framework", "Kotlin Multiplatform")
                InfoRow("Compose", "Multiplatform 1.10.0")
                InfoRow("License", "MIT")
            }
        }

        Text(
            "Supported Platforms",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                listOf(
                    "🤖 Android (API 24+)",
                    "🍎 iOS (13.0+)",
                    "🖥️ Desktop (JVM 11+)",
                    "🌐 Web (WASM/JS)"
                ).forEach { platform ->
                    Text(
                        platform,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Text(
            "Features",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                listOf(
                    "✅ Complete PNG/APNG format parsing",
                    "✅ Cross-platform frame decoding",
                    "✅ Animation playback control",
                    "✅ Multi-source resource loading",
                    "✅ Network APNG loading",
                    "✅ LRU disk caching",
                    "✅ Error handling and recovery"
                ).forEach { feature ->
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ApngCard(resource: ApngResource) {
    var apngData by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Load APNG data from resources
    LaunchedEffect(resource.fileName) {
        isLoading = true
        loadError = null
        try {
            val bytes = Res.readBytes("files/${resource.fileName}")
            apngData = bytes
            isLoading = false
        } catch (e: Exception) {
            loadError = e.message ?: "加载失败"
            isLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // APNG Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    loadError != null -> {
                        Text(
                            text = "⚠️ ${loadError?.take(20)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    apngData != null -> {
                        ApngImage(
                            data = apngData!!,
                            contentDescription = resource.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            autoPlay = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = resource.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            Text(
                text = resource.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
