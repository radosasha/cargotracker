package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for connect load request
 * Matches the server's ConnectLoadRequest.java structure
 */
@Serializable
data class ConnectLoadRequest(
    @SerialName("loadId")
    val loadId: String,
)

