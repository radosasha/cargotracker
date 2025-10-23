package com.shiplocate.data.mapper

import com.shiplocate.core.database.entity.LoadEntity
import com.shiplocate.data.network.dto.load.LoadDto
import com.shiplocate.domain.model.load.Load

fun LoadDto.toDomain(): Load {
    return Load(
        loadId = loadId,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus,
    )
}

fun LoadDto.toEntity(): LoadEntity {
    return LoadEntity(
        loadId = loadId,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus,
    )
}

fun LoadEntity.toDomain(): Load {
    return Load(
        loadId = loadId,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus,
    )
}
