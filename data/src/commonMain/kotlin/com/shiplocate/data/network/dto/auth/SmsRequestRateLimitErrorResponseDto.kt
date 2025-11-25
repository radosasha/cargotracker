package com.shiplocate.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Rate limit error response DTO for SMS request endpoint (POST /api/mobile/auth/request)
 * Used specifically for HTTP 429 responses
 * 
 * Error code: RATE_LIMIT_EXCEEDED
 * Message: "Too many SMS requests. Please try again later."
 */
@Serializable
data class SmsRequestRateLimitErrorResponseDto(
    val error: String,
    val message: String,
    val timestamp: Long? = null,
    val retryAfterSeconds: Long? = null,
    val nextRetryAt: String? = null,
)


