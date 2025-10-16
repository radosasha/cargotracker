package com.tracker.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for SMS code verification
 * Maps to backend: POST /api/mobile/auth/verify
 */
@Serializable
data class SmsVerifyDto(
    val phone: String,
    val code: String,
    val deviceInfo: String? = null
)




