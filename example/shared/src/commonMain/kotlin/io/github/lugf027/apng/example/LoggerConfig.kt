package io.github.lugf027.apng.example

import io.github.lugf027.apng.logger.ApngLogger
import io.github.lugf027.apng.logger.LogImpl
import io.github.lugf027.apng.logger.LogLevel

/**
 * Logger configuration for the APNG example app.
 * 
 * This object provides methods to initialize and configure the APNG library logging.
 */
object LoggerConfig {
    
    /**
     * Initialize the logger with default settings.
     * Uses DEBUG level by default.
     */
    fun init() {
        init(LogLevel.DEBUG)
    }
    
    /**
     * Initialize the logger with a specific log level.
     * 
     * @param level The minimum log level to display
     */
    fun init(level: LogLevel) {
        ApngLogger.setEnabled(true)
        ApngLogger.setLogLevel(level)
        ApngLogger.i("LoggerConfig", "APNG Logger initialized with level: $level")
    }
    
    /**
     * Initialize the logger with a custom log implementation.
     * 
     * @param impl The custom log implementation
     * @param level The minimum log level to display (default: DEBUG)
     */
    fun init(impl: LogImpl, level: LogLevel = LogLevel.DEBUG) {
        ApngLogger.setLogImpl(impl)
        ApngLogger.setEnabled(true)
        ApngLogger.setLogLevel(level)
        ApngLogger.i("LoggerConfig", "APNG Logger initialized with custom impl, level: $level")
    }
    
    /**
     * Set the log level.
     * 
     * @param level The minimum log level to display
     */
    fun setLogLevel(level: LogLevel) {
        ApngLogger.setLogLevel(level)
        ApngLogger.i("LoggerConfig", "Log level changed to: $level")
    }
    
    /**
     * Enable or disable logging.
     * 
     * @param enabled Whether logging should be enabled
     */
    fun setEnabled(enabled: Boolean) {
        ApngLogger.setEnabled(enabled)
        if (enabled) {
            ApngLogger.i("LoggerConfig", "Logging enabled")
        }
    }
    
    /**
     * Set a tag filter to only show logs from specific tags.
     * 
     * @param filter The filter predicate, or null to disable filtering
     */
    fun setTagFilter(filter: ((String) -> Boolean)?) {
        ApngLogger.setTagFilter(filter)
    }
    
    /**
     * Convenience method to only show logs from APNG library tags.
     */
    fun filterApngTagsOnly() {
        ApngLogger.setTagFilter { tag -> tag.startsWith("Apng") }
        ApngLogger.i("LoggerConfig", "Tag filter set to Apng* only")
    }
    
    /**
     * Reset logger to default settings.
     */
    fun reset() {
        ApngLogger.reset()
        ApngLogger.i("LoggerConfig", "Logger reset to defaults")
    }
}
