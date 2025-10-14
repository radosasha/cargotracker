package com.tracker.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for SMS code request response
 */
@Serializable
data class SmsRequestResponseDto(
    val status: String,
    val message: String
)






