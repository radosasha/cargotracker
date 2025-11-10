package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for EnterStopRequest
 * Matches the server's EnterStopRequest.java structure
 */
@Serializable
data class EnterStopRequest(
    @SerialName("stopId")
    val stopId: Long,
)

