package com.tracker.data.mapper

import com.tracker.core.database.entity.LoadEntity
import com.tracker.data.network.dto.load.LoadDto
import com.tracker.domain.model.load.Load

/**
 * Mapper functions for Load conversions between layers
 */

/**
 * Convert LoadDto to domain Load model
 */
fun LoadDto.toDomain(): Load {
    return Load(
        loadId = loadId,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt
    )
}

/**
 * Convert LoadDto to LoadEntity for caching
 */
fun LoadDto.toEntity(): LoadEntity {
    return LoadEntity(
        loadId = loadId,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt
    )
}

/**
 * Convert LoadEntity to domain Load model
 */
fun LoadEntity.toDomain(): Load {
    return Load(
        loadId = loadId,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt
    )
}


