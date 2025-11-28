package com.shiplocate.data.network.dto.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Message from API response
 * Matches the server's ChatMessageDto.java structure
 */
@Serializable
data class MessageDto(
    @SerialName("id")
    val id: Long,
    @SerialName("loadId")
    val loadId: Long,
    @SerialName("message")
    val message: String,
    @SerialName("type")
    val type: Int, // 0 = DISPATCHER, 1 = DRIVER
    @SerialName("createdAt")
    val createdAt: Long, // Unix timestamp in milliseconds
)

