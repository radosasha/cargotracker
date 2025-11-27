package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for UpdateStopCompletionRequest
 * Matches the server's UpdateStopCompletionRequest.java structure
 */
@Serializable
data class UpdateStopCompletionRequest(
    @SerialName("stopId")
    val stopId: Long,
    @SerialName("completion")
    val completion: Int, // 0 = NOT_COMPLETED, 1 = COMPLETED
)

