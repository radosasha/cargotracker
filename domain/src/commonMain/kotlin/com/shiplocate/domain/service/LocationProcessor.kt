package com.shiplocate.domain.service

import com.shiplocate.domain.model.Location
import com.shiplocate.domain.model.TrackingStats

/**
 * Интерфейс для обработки и фильтрации GPS координат
 * Содержит бизнес-логику определения качества и необходимости отправки координат
 */
interface LocationProcessor {
    /**
     * Обрабатывает новую GPS координату
     * @param location новая координата
     * @return результат обработки с информацией о том, была ли координата отправлена
     */
    fun processLocation(location: Location): LocationProcessResult

    /**
     * Обновляет статистику сохранения координаты
     */
    fun updateSavedLocation()

    /**
     * Обновляет статистику отправки координат
     */
    fun updateSentLocations(
        location: Location,
        count: Int,
    )

    /**
     * Обновляет статистику ошибки отправки координат
     */
    fun updateSendError(
        location: Location,
        errorMessage: String,
        errorType: String = "Network Error",
    )

    /**
     * Создает текущую статистику трекинга
     */
    fun createCurrentTrackingStats(): TrackingStats
}

/**
 * Результат обработки GPS координаты
 */
data class LocationProcessResult(
    val shouldSend: Boolean,
    val reason: String,
    val totalReceived: Int,
    val totalSent: Int,
    val lastSentTime: Long,
    val trackingStats: TrackingStats,
    val lastCoordinateLat: Double,
    val lastCoordinateLon: Double,
    val lastCoordinateTime: Long,
    val coordinateErrorMeters: Int,
)
