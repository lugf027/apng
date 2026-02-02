package io.github.lugf027.apng

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lugf027.apng.compose.ApngLoadState
import io.github.lugf027.apng.compose.rememberApngState

@Composable
fun ApngDemoScreen() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "APNG Demo",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Demo 1: Placeholder APNG
            DemoSection(
                title = "APNG 动画库 v1.0",
                description = "Kotlin Multiplatform APNG 解析和渲染库"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Demo 2: Features
            Text(
                "支持特性",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FeatureItem("✓ 多平台支持 (Android, iOS, Desktop, Web)")
            FeatureItem("✓ APNG 格式解析")
            FeatureItem("✓ 动画播放控制")
            FeatureItem("✓ 帧缓存管理")
            FeatureItem("✓ 性能优化")

            Spacer(modifier = Modifier.height(24.dp))

            // Demo 3: Platforms
            Text(
                "支持平台",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PlatformGrid()

            Spacer(modifier = Modifier.height(24.dp))

            // Demo 4: Status
            Text(
                "项目状态",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            StatusItem("apng-core", "PNG/APNG 解析库", true)
            StatusItem("apng-compose", "Compose 组件库", true)
            StatusItem("演示应用", "集成示例", true)
        }
    }
}

@Composable
private fun DemoSection(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun PlatformGrid() {
    val platforms = listOf("Android", "iOS", "Desktop", "Web")
    Column(modifier = Modifier.fillMaxWidth()) {
        for (i in platforms.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth()) {
                PlatformCard(
                    platforms[i],
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                if (i + 1 < platforms.size) {
                    PlatformCard(
                        platforms[i + 1],
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlatformCard(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun StatusItem(label: String, description: String, isReady: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
