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
    @SerialName("locationName")
    val locationName: String,
    @SerialName("locationAddress")
    val locationAddress: String,
    @SerialName("date")
    val date: Long,
    @SerialName("geofenceRadius")
    val geofenceRadius: Int,
    @SerialName("stopIndex")
    val index: Int,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("enter")
    val enter: Boolean,
    @SerialName("note")
    val note: String? = null,
    @SerialName("completion")
    val completion: Int = 0, // 0 = NOT_COMPLETED, 1 = COMPLETED
)

