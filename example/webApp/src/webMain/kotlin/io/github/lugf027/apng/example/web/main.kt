package io.github.lugf027.apng.example.web

import io.github.lugf027.apng.example.App
import io.github.lugf027.apng.example.LoggerConfig
import io.github.lugf027.apng.logger.LogLevel

fun main() {
    // Initialize APNG logger with DEBUG level for Web
    LoggerConfig.init(LogLevel.DEBUG)
    
    CompatComposeWindow {
        App()
    }
}
