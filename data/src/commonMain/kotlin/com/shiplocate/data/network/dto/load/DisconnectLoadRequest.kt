package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for disconnect load request
 * Matches the server's DisconnectLoadRequest.java structure
 */
@Serializable
data class DisconnectLoadRequest(
    @SerialName("loadId")
    val serverLoadId: Long,
)

