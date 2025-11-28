package com.shiplocate.data.network.dto.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for sending message request
 * Matches the server's SendChatMessageRequest.java structure
 */
@Serializable
data class SendMessageRequest(
    @SerialName("loadId")
    val loadId: Long,
    @SerialName("message")
    val message: String,
)

