package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Load from API response
 * Matches the server's LoadDto.java structure
 */
@Serializable
data class LoadDto(
    @SerialName("loadId")
    val loadId: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("lastUpdated")
    val lastUpdated: Long? = null,
    @SerialName("createdAt")
    val createdAt: Long,
    @SerialName("loadStatus")
    val loadStatus: Int,
)
