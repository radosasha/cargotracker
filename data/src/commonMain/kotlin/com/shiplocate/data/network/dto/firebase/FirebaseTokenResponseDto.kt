package com.shiplocate.data.network.dto.firebase

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseTokenResponseDto(
    val success: Boolean,
    val message: String,
)

@Serializable
data class FirebaseTokenStatusDto(
    val hasToken: Boolean,
    val token: String?,
)
