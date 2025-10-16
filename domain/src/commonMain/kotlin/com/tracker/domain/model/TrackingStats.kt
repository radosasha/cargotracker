package com.tracker.domain.model

import kotlinx.datetime.Instant

/**
 * Статистика GPS трекинга для отображения в уведомлениях
 */
data class TrackingStats(
    val lastFilteredLocation: FilteredLocationInfo? = null,
    val lastSentLocation: LocationInfo? = null,
    val lastSendError: SendErrorInfo? = null,
    val isTracking: Boolean = false,
    val totalSaved: Int = 0,
    val totalSent: Int = 0,
    val totalFiltered: Int = 0,
)

/**
 * Информация о конкретной GPS координате
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Instant,
    val speed: Float? = null,
    val altitude: Double? = null,
)

/**
 * Информация об отфильтрованной GPS координате
 */
data class FilteredLocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Instant,
    val speed: Float? = null,
    val altitude: Double? = null,
    val filterReason: String,
)

/**
 * Информация об ошибке отправки координат на сервер
 */
data class SendErrorInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Instant,
    val speed: Float? = null,
    val altitude: Double? = null,
    val errorMessage: String,
    val errorType: String = "Network Error",
)
