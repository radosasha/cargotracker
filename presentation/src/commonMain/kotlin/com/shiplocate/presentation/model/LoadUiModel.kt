package com.shiplocate.presentation.model

import com.shiplocate.domain.model.load.Stop

/**
 * UI модель для Load
 * Используется в Presentation слое вместо доменной модели Load
 */
data class LoadUiModel(
    val id: Long,
    val serverId: Long,
    val loadName: String,
    val description: String?,
    val lastUpdated: Long?,
    val createdAt: Long,
    val loadStatus: LoadStatus,
    val stops: List<Stop> = emptyList(),
)

