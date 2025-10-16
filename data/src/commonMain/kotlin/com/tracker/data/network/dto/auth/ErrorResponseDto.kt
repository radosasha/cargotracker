package com.tracker.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for API error responses
 * Covers all error types:
 * - ErrorResponse (base)
 * - RateLimitErrorResponse (with retryAfterSeconds, nextRetryAt)
 * - VerificationErrorResponse (with remainingAttempts)
 */
@Serializable
data class ErrorResponseDto(
    val error: String,
    val message: String,
    val timestamp: Long? = null,
    // RateLimitErrorResponse fields
    val retryAfterSeconds: Long? = null,
    val nextRetryAt: String? = null,
    // VerificationErrorResponse fields
    val remainingAttempts: Int? = null,
)
