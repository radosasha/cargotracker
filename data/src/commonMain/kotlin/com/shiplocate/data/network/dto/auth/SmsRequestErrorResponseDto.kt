package com.shiplocate.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Error response DTO for SMS request endpoint (POST /api/mobile/auth/request)
 * Used for HTTP 400, 500, 503 responses
 * 
 * Possible error codes:
 * - VALIDATION_ERROR: Invalid request data (400)
 * - DATABASE_ERROR: Internal database error (500)
 * - SMS_SERVICE_ERROR: SMS service unavailable (503)
 * 
 * Note: For HTTP 429 (RATE_LIMIT_EXCEEDED), use SmsRequestRateLimitErrorResponseDto
 */
@Serializable
data class SmsRequestErrorResponseDto(
    val error: String,
    val message: String,
    val timestamp: Long? = null,
)

