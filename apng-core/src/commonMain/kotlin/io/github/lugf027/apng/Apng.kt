package io.github.lugf027.apng

public object Apng {
    public const val IterateForever: Int = Int.MAX_VALUE

    public var logger: ApngLogger? = ApngLogger.Default
}

public interface ApngLogger {
    public fun warn(message: String, throwable: Throwable? = null)
    public fun error(message: String, throwable: Throwable? = null)

    public companion object {
        public val Default: ApngLogger = object : ApngLogger {
            override fun warn(message: String, throwable: Throwable?) {
                println("APNG Warning: $message")
                throwable?.printStackTrace()
            }

            override fun error(message: String, throwable: Throwable?) {
                println("APNG Error: $message")
                throwable?.printStackTrace()
            }
        }
    }
}
