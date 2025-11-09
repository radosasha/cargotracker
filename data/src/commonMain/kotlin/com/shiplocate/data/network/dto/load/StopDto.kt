package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Stop (Drop) from API response
 * Matches the server's DropDto.java structure
 */
@Serializable
data class StopDto(
    @SerialName("id")
    val id: Long,
    @SerialName("type")
    val type: Int,
    @SerialName("locationAddress")
    val locationAddress: String,
    @SerialName("date")
    val date: Long,
    @SerialName("geofenceRadius")
    val geofenceRadius: Int,
    @SerialName("index")
    val index: Int,
)

