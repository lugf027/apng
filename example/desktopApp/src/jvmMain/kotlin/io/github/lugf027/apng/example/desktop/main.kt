package io.github.lugf027.apng.example.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import io.github.lugf027.apng.example.App
import io.github.lugf027.apng.example.LoggerConfig
import io.github.lugf027.apng.logger.LogLevel

fun main() {
    // Initialize APNG logger with DEBUG level for Desktop
    LoggerConfig.init(LogLevel.DEBUG)
    
    singleWindowApplication(
        title = "APNG Demo",
        state = WindowState(
            size = DpSize(1024.dp, 720.dp)
        )
    ) {
        App()
    }
}
