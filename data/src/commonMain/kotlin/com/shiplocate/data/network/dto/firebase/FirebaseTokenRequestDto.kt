package com.shiplocate.data.network.dto.firebase

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseTokenRequestDto(
    val firebaseToken: String,
)
