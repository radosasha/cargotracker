package com.shiplocate.presentation.mapper

import com.shiplocate.domain.model.load.Load
import com.shiplocate.presentation.model.LoadUiModel
import com.shiplocate.presentation.model.toLoadStatus

/**
 * Mapper для преобразования доменной модели Load в UI модель LoadUiModel
 */
fun Load.toUiModel(): LoadUiModel {
    return LoadUiModel(
        id = id,
        serverId = serverId,
        loadName = loadName,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus.toLoadStatus(),
        stops = stops,
    )
}

