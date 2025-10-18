package com.shiplocate.data.network.dto.auth

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for mobile user
 */
@Serializable
data class MobileUserDto(
    val id: Long,
    val phone: String,
    val name: String,
)
