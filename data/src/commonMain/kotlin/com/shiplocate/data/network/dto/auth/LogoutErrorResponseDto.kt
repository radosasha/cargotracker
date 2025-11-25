package com.shiplocate.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Error response DTO for logout endpoint (POST /api/mobile/auth/logout)
 * 
 * Possible error codes:
 * - INVALID_REQUEST: Bearer token required
 * - INVALID_TOKEN: Invalid or expired token
 * - DATABASE_ERROR: Internal database error
 * - INTERNAL_ERROR: Unexpected error
 * - SUCCESS: Logged out successfully (200 OK)
 */
@Serializable
data class LogoutErrorResponseDto(
    val error: String,
    val message: String,
    val timestamp: Long? = null,
)

