package com.shiplocate.presentation.model

import com.shiplocate.domain.model.load.Stop

/**
 * UI модель для активного Load
 * Используется для отображения активного Load на вкладке Active
 */
data class ActiveLoadUiModel(
    val id: Long,
    val serverId: Long,
    val loadName: String,
    val description: String?,
    val lastUpdated: Long?,
    val createdAt: Long,
    val loadStatus: LoadStatus,
    val stops: List<Stop> = emptyList(),
    val routeDuration: String? = null, // ETA from route
)

