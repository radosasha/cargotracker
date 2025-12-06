package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for route coordinate (start point)
 * Matches server's CoordinateDto structure used in RouteRequest
 * Contains only lat and lon fields
 */
@Serializable
data class RouteCoordinateDto(
    @SerialName("lat")
    val lat: Double,
    @SerialName("lon")
    val lon: Double,
)

