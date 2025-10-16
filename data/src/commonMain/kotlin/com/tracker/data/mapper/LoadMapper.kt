package com.tracker.data.mapper

import com.tracker.core.database.entity.LoadEntity
import com.tracker.data.network.dto.load.LoadDto
import com.tracker.domain.model.load.Load

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
