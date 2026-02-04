package io.github.lugf027.apng.logger

/**
 * Log level enum for APNG library logging.
 * 
 * Log levels are ordered from lowest to highest priority:
 * VERBOSE < DEBUG < INFO < WARN < ERROR < NONE
 */
enum class LogLevel(val priority: Int) {
    /**
     * Verbose level - Most detailed logging for debugging purposes.
     * Use for fine-grained information like individual chunk parsing.
     */
    VERBOSE(1),
    
    /**
     * Debug level - Detailed information for debugging.
     * Use for general debugging information like frame decoding progress.
     */
    DEBUG(2),
    
    /**
     * Info level - General information about library operations.
     * Use for significant events like parsing start/complete, loading success.
     */
    INFO(3),
    
    /**
     * Warn level - Warning messages for potentially problematic situations.
     * Use for recoverable errors or deprecated usage.
     */
    WARN(4),
    
    /**
     * Error level - Error messages for serious problems.
     * Use for exceptions, parsing failures, or critical errors.
     */
    ERROR(5),
    
    /**
     * None level - Disable all logging.
     */
    NONE(Int.MAX_VALUE)
}
