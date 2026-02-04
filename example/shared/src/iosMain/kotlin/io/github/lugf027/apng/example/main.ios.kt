package io.github.lugf027.apng.example

import androidx.compose.ui.window.ComposeUIViewController
import io.github.lugf027.apng.logger.LogLevel

fun MainViewController() = ComposeUIViewController { 
    // Initialize APNG logger with DEBUG level for iOS
    LoggerConfig.init(LogLevel.DEBUG)
    
    App() 
}
