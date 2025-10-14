package com.tracker.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for authentication response
 */
@Serializable
data class AuthResponseDto(
    val token: String,
    val user: MobileUserDto
)





