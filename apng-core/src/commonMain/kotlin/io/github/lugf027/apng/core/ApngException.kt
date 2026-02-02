package io.github.lugf027.apng.core

/**
 * APNG 解析异常
 */
open class ApngException(message: String, cause: Throwable? = null) : Exception(message, cause)

class InvalidPngSignatureException(message: String = "Invalid PNG signature") : ApngException(message)

class InvalidChunkException(message: String) : ApngException(message)

class InvalidApngException(message: String) : ApngException(message)

class DecodingException(message: String, cause: Throwable? = null) : ApngException(message, cause)
