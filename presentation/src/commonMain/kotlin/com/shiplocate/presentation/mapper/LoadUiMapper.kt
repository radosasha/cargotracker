package com.shiplocate.presentation.mapper

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.LoadStatus as DomainLoadStatus
import com.shiplocate.presentation.model.ActiveLoadUiModel
import com.shiplocate.presentation.model.LoadStatus as PresentationLoadStatus
import com.shiplocate.presentation.model.LoadUiModel

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
        loadStatus = loadStatus.toPresentationLoadStatus(),
        stops = stops,
    )
}

/**
 * Mapper для преобразования доменной модели Load в UI модель ActiveLoadUiModel
 */
fun Load.toActiveLoadUiModel(routeDuration: String? = null): ActiveLoadUiModel {
    return ActiveLoadUiModel(
        id = id,
        serverId = serverId,
        loadName = loadName,
        description = description,
        lastUpdated = lastUpdated,
        createdAt = createdAt,
        loadStatus = loadStatus.toPresentationLoadStatus(),
        stops = stops,
        routeDuration = routeDuration,
    )
}

/**
 * Конвертирует Domain LoadStatus в Presentation LoadStatus
 */
fun DomainLoadStatus.toPresentationLoadStatus(): PresentationLoadStatus {
    return when (this) {
        DomainLoadStatus.LOAD_STATUS_NOT_CONNECTED -> PresentationLoadStatus.LOAD_STATUS_NOT_CONNECTED
        DomainLoadStatus.LOAD_STATUS_CONNECTED -> PresentationLoadStatus.LOAD_STATUS_CONNECTED
        DomainLoadStatus.LOAD_STATUS_DISCONNECTED -> PresentationLoadStatus.LOAD_STATUS_DISCONNECTED
        DomainLoadStatus.LOAD_STATUS_REJECTED -> PresentationLoadStatus.LOAD_STATUS_REJECTED
        DomainLoadStatus.LOAD_STATUS_UNKNOWN -> PresentationLoadStatus.LOAD_STATUS_UNKNOWN
    }
}

