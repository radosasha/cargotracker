package com.tracker.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for SMS code request
 * Maps to backend: POST /api/mobile/auth/request
 */
@Serializable
data class SmsRequestDto(
    val phone: String
)






