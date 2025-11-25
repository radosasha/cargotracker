package com.shiplocate.data.network.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class SmsVerifyErrorResponseDto(
    val error: String,
    val message: String,
    val timestamp: Long? = null,
    // VerificationErrorResponse fields (for CODE_INVALID)
    val remainingAttempts: Int? = null,
    // TwilioErrorResponse fields (for RATE_LIMIT_EXCEEDED, INVALID_PHONE, VERIFICATION_ERROR)
    val twilioErrorCode: String? = null,
)

