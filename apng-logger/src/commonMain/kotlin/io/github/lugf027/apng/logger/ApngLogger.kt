package io.github.lugf027.apng.logger

import kotlin.concurrent.Volatile

/**
 * Central logging facade for the APNG library.
 * 
 * This class provides a unified logging interface that supports:
 * - External log implementation injection via [setLogImpl]
 * - Configurable log levels via [setLogLevel]
 * - Tag-based filtering via [setTagFilter]
 * 
 * ## Quick Start
 * 
 * ```kotlin
 * // Set custom log implementation (optional, defaults to println)
 * ApngLogger.setLogImpl(myCustomLogImpl)
 * 
 * // Set minimum log level (optional, defaults to DEBUG)
 * ApngLogger.setLogLevel(LogLevel.INFO)
 * 
 * // Optionally set tag filter
 * ApngLogger.setTagFilter { tag -> tag.startsWith("Apng") }
 * ```
 * 
 * ## Predefined Tags
 * 
 * The library uses the following tags:
 * - `ApngParser` - APNG file parsing operations
 * - `ApngLoader` - APNG loading operations
 * - `ApngFrame` - Frame decoding and processing
 * - `ApngCompose` - Compose UI integration
 * - `ApngNetwork` - Network operations
 * - `ApngCache` - Cache operations
 * - `ApngAnimation` - Animation playback
 */
object ApngLogger {
    
    // Default tag for the library
    private const val DEFAULT_TAG = "Apng"
    
    // Current log implementation
    @Volatile
    private var logImpl: LogImpl = DefaultLogImpl
    
    // Current minimum log level
    @Volatile
    private var minLogLevel: LogLevel = LogLevel.DEBUG
    
    // Optional tag filter
    @Volatile
    private var tagFilter: ((String) -> Boolean)? = null
    
    // Enable/disable flag
    @Volatile
    private var isEnabled: Boolean = true
    
    /**
     * Set a custom log implementation.
     * 
     * @param impl The log implementation to use
     */
    fun setLogImpl(impl: LogImpl) {
        logImpl = impl
    }
    
    /**
     * Get the current log implementation.
     * 
     * @return The current log implementation
     */
    fun getLogImpl(): LogImpl = logImpl
    
    /**
     * Set the minimum log level.
     * Messages below this level will be filtered out.
     * 
     * @param level The minimum log level
     */
    fun setLogLevel(level: LogLevel) {
        minLogLevel = level
    }
    
    /**
     * Get the current minimum log level.
     * 
     * @return The current minimum log level
     */
    fun getLogLevel(): LogLevel = minLogLevel
    
    /**
     * Set a tag filter predicate.
     * Only tags matching this filter will be logged.
     * 
     * @param filter The filter predicate, or null to disable filtering
     */
    fun setTagFilter(filter: ((String) -> Boolean)?) {
        tagFilter = filter
    }
    
    /**
     * Enable or disable logging globally.
     * 
     * @param enabled Whether logging should be enabled
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    /**
     * Check if logging is enabled.
     * 
     * @return Whether logging is enabled
     */
    fun isEnabled(): Boolean = isEnabled
    
    /**
     * Reset logger to default settings.
     */
    fun reset() {
        logImpl = DefaultLogImpl
        minLogLevel = LogLevel.DEBUG
        tagFilter = null
        isEnabled = true
    }
    
    // Internal logging methods
    
    @PublishedApi
    internal fun shouldLog(level: LogLevel, tag: String): Boolean {
        if (!isEnabled) return false
        if (level.priority < minLogLevel.priority) return false
        val filter = tagFilter
        if (filter != null && !filter(tag)) return false
        return true
    }
    
    @PublishedApi
    internal fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(level, tag)) {
            logImpl.log(level, tag, message, throwable)
        }
    }
    
    // Public logging methods
    
    /**
     * Log a verbose message.
     */
    fun v(tag: String, message: String) {
        log(LogLevel.VERBOSE, tag, message)
    }
    
    /**
     * Log a verbose message with lazy evaluation.
     */
    inline fun v(tag: String, message: () -> String) {
        if (shouldLog(LogLevel.VERBOSE, tag)) {
            log(LogLevel.VERBOSE, tag, message())
        }
    }
    
    /**
     * Log a debug message.
     */
    fun d(tag: String, message: String) {
        log(LogLevel.DEBUG, tag, message)
    }
    
    /**
     * Log a debug message with lazy evaluation.
     */
    inline fun d(tag: String, message: () -> String) {
        if (shouldLog(LogLevel.DEBUG, tag)) {
            log(LogLevel.DEBUG, tag, message())
        }
    }
    
    /**
     * Log an info message.
     */
    fun i(tag: String, message: String) {
        log(LogLevel.INFO, tag, message)
    }
    
    /**
     * Log an info message with lazy evaluation.
     */
    inline fun i(tag: String, message: () -> String) {
        if (shouldLog(LogLevel.INFO, tag)) {
            log(LogLevel.INFO, tag, message())
        }
    }
    
    /**
     * Log a warning message.
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, tag, message, throwable)
    }
    
    /**
     * Log a warning message with lazy evaluation.
     */
    inline fun w(tag: String, throwable: Throwable? = null, message: () -> String) {
        if (shouldLog(LogLevel.WARN, tag)) {
            log(LogLevel.WARN, tag, message(), throwable)
        }
    }
    
    /**
     * Log an error message.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }
    
    /**
     * Log an error message with lazy evaluation.
     */
    inline fun e(tag: String, throwable: Throwable? = null, message: () -> String) {
        if (shouldLog(LogLevel.ERROR, tag)) {
            log(LogLevel.ERROR, tag, message(), throwable)
        }
    }
}

/**
 * Predefined log tags used by the APNG library.
 */
object ApngLogTags {
    /** Tag for APNG parsing operations */
    const val PARSER = "ApngParser"
    
    /** Tag for APNG loading operations */
    const val LOADER = "ApngLoader"
    
    /** Tag for frame decoding operations */
    const val FRAME = "ApngFrame"
    
    /** Tag for Compose UI integration */
    const val COMPOSE = "ApngCompose"
    
    /** Tag for network operations */
    const val NETWORK = "ApngNetwork"
    
    /** Tag for cache operations */
    const val CACHE = "ApngCache"
    
    /** Tag for animation playback */
    const val ANIMATION = "ApngAnimation"
    
    /** Tag for chunk parsing */
    const val CHUNK = "ApngChunk"
}
