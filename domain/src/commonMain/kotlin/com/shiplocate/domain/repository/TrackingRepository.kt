package com.shiplocate.domain.repository

import com.shiplocate.domain.model.TrackingStatus

/**
 * Интерфейс репозитория для управления GPS трекингом
 */
interface TrackingRepository {
    /**
     * Запускает GPS трекинг
     */
    suspend fun startTracking(): Result<Unit>

    /**
     * Останавливает GPS трекинг
     */
    suspend fun stopTracking(): Result<Unit>

    /**
     * Получает текущий статус трекинга
     */
    suspend fun getTrackingStatus(): TrackingStatus
}
