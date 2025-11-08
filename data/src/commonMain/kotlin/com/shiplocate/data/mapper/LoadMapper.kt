package com.shiplocate.data.mapper

import com.shiplocate.core.database.entity.LoadEntity
import com.shiplocate.data.network.dto.load.LoadDto
import com.shiplocate.domain.model.load.Load

fun LoadDto.toDomain(): Load {
    return Load(
        id = id, // Internal ID for application operations
        serverId = id, // serverId используется для API вызовов (must match server's ID)
        loadName = loadName, // Name for UI display
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus,
    )
}

fun LoadDto.toEntity(): LoadEntity {
    return LoadEntity(
        loadName = loadName, // Name for UI display
        id = id, // Internal ID for application operations
        serverId = id, // serverId используется для API вызовов (must match server's ID)
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus,
    )
}

fun LoadEntity.toDomain(): Load {
    return Load(
        id = id,
        serverId = serverId,
        loadName = loadName,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus,
    )
}
