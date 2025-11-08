package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for ping load request
 * Matches the server's PingLoadRequest.java structure
 */
@Serializable
data class PingLoadRequest(
    @SerialName("loadId")
    val loadId: Long,
)

