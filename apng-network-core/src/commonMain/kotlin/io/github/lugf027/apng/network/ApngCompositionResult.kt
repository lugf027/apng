package io.github.lugf027.apng.network

import io.github.lugf027.apng.core.ApngImage

/**
 * Represents the result of an asynchronous APNG composition loading operation.
 */
sealed interface ApngCompositionResult {
    /**
     * Loading state with optional progress information.
     *
     * @param progress Loading progress from 0.0 to 1.0, or null if unknown
     */
    data class Loading(val progress: Float? = null) : ApngCompositionResult

    /**
     * Successfully loaded APNG composition.
     *
     * @param composition The loaded APNG image
     */
    data class Success(val composition: ApngImage) : ApngCompositionResult

    /**
     * Loading failed with an exception.
     *
     * @param exception The error that occurred
     */
    data class Error(val exception: Exception) : ApngCompositionResult
}
