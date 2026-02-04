package io.github.lugf027.apng.logger

/**
 * Interface for external log implementation injection.
 * 
 * Users can provide their own logging implementation by implementing this interface
 * and registering it via [ApngLogger.setLogImpl].
 * 
 * Example usage:
 * ```kotlin
 * // Android implementation using Logcat
 * object AndroidLogImpl : LogImpl {
 *     override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
 *         when (level) {
 *             LogLevel.VERBOSE -> Log.v(tag, message, throwable)
 *             LogLevel.DEBUG -> Log.d(tag, message, throwable)
 *             LogLevel.INFO -> Log.i(tag, message, throwable)
 *             LogLevel.WARN -> Log.w(tag, message, throwable)
 *             LogLevel.ERROR -> Log.e(tag, message, throwable)
 *             LogLevel.NONE -> { /* no-op */ }
 *         }
 *     }
 * }
 * 
 * // Register the implementation
 * ApngLogger.setLogImpl(AndroidLogImpl)
 * ```
 */
interface LogImpl {
    /**
     * Log a message with the specified level.
     * 
     * @param level The log level
     * @param tag The tag identifying the source of the log
     * @param message The log message
     * @param throwable Optional throwable for error logging
     */
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Default log implementation that prints to standard output.
 * This is used when no custom implementation is provided.
 */
internal object DefaultLogImpl : LogImpl {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        if (level == LogLevel.NONE) return
        
        val levelStr = when (level) {
            LogLevel.VERBOSE -> "V"
            LogLevel.DEBUG -> "D"
            LogLevel.INFO -> "I"
            LogLevel.WARN -> "W"
            LogLevel.ERROR -> "E"
            LogLevel.NONE -> return
        }
        
        val logMessage = "[$levelStr/$tag] $message"
        
        if (level == LogLevel.ERROR || level == LogLevel.WARN) {
            println(logMessage)
            throwable?.printStackTrace()
        } else {
            println(logMessage)
        }
    }
}

/**
 * A no-op log implementation that discards all log messages.
 * Useful for production environments where logging should be completely disabled.
 */
object NoOpLogImpl : LogImpl {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        // No-op: intentionally empty
    }
}
