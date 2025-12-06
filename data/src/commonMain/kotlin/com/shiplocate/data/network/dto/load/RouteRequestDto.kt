package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for route request to mobile API
 * Matches server's RouteRequest structure
 */
@Serializable
data class RouteRequestDto(
    @SerialName("loadId")
    val loadId: Long,
    @SerialName("start")
    val start: RouteCoordinateDto,
)

