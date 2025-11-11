package com.shiplocate.data.mapper

import com.shiplocate.core.database.entity.LoadEntity
import com.shiplocate.core.database.entity.StopEntity
import com.shiplocate.data.network.dto.load.LoadDto
import com.shiplocate.data.network.dto.load.StopDto
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.Stop

fun LoadDto.toDomain(): Load {
    return Load(
        id = id, // Internal ID for application operations
        serverId = id, // serverId используется для API вызовов (must match server's ID)
        loadName = loadName, // Name for UI display
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus,
        stops = stops.map { it.toDomain() },
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

fun LoadDto.toStopEntities(loadId: Long): List<StopEntity> {
    return stops.map { stopDto ->
        StopEntity(
            loadId = loadId,
            serverId = stopDto.id,
            type = stopDto.type,
            locationName = stopDto.locationName,
            locationAddress = stopDto.locationAddress,
            date = stopDto.date,
            geofenceRadius = stopDto.geofenceRadius,
            stopIndex = stopDto.index,
            latitude = stopDto.latitude,
            longitude = stopDto.longitude,
            enter = stopDto.enter
        )
    }
}

fun StopDto.toDomain(): Stop {
    return Stop(
        id = id,
        type = type,
        locationName = locationName,
        locationAddress = locationAddress,
        date = date,
        geofenceRadius = geofenceRadius,
        index = index,
        latitude = latitude,
        longitude = longitude,
        enter = enter
    )
}

fun StopDto.toStopEntity(loadId: Long): StopEntity {
    return StopEntity(
        loadId = loadId,
        serverId = id,
        type = type,
        locationName = locationName,
        locationAddress = locationAddress,
        date = date,
        geofenceRadius = geofenceRadius,
        stopIndex = index,
        latitude = latitude,
        longitude = longitude,
        enter = enter
    )
}

fun StopEntity.toDomain(): Stop {
    return Stop(
        id = serverId,
        type = type,
        locationName = locationName,
        locationAddress = locationAddress,
        date = date,
        geofenceRadius = geofenceRadius,
        index = stopIndex,
        latitude = latitude,
        longitude = longitude,
        enter = enter
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
        stops = emptyList(), // Stops загружаются отдельно через StopsLocalDataSource
    )
}
