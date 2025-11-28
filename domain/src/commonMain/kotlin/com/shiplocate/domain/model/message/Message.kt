package com.shiplocate.domain.model.message

/**
 * Domain model for Message
 */
data class Message(
    val id: Long, // Server's message ID (0 for unsent messages, > 0 for sent messages)
    val loadId: Long,
    val message: String,
    val type: Int, // 0 = DISPATCHER, 1 = DRIVER
    val datetime: Long, // Unix timestamp in milliseconds
) {
    companion object {
        const val MESSAGE_TYPE_DISPATCHER = 0
        const val MESSAGE_TYPE_DRIVER = 1
    }
}

