package com.shiplocate.data.mapper

import com.shiplocate.core.database.entity.MessageEntity
import com.shiplocate.data.network.dto.message.MessageDto
import com.shiplocate.domain.model.message.Message

/**
 * Mappers for Message DTOs <-> Domain models <-> Entities
 */

fun MessageDto.toMessageDomain(): Message {
    return Message(
        id = id,
        loadId = loadId,
        message = message,
        type = type,
        datetime = createdAt,
    )
}

fun MessageDto.toMessageEntity(loadId: Long): MessageEntity {
    return MessageEntity(
        loadId = loadId,
        serverId = id,
        message = message,
        type = type,
        datetime = createdAt,
    )
}

fun MessageEntity.toMessageDomain(): Message {
    return Message(
        id = serverId,
        loadId = loadId,
        message = message,
        type = type,
        datetime = datetime,
    )
}

fun Message.toMessageEntity(loadId: Long): MessageEntity {
    return MessageEntity(
        loadId = loadId,
        serverId = id,
        message = message,
        type = type,
        datetime = datetime,
    )
}

