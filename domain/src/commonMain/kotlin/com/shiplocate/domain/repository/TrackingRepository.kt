package com.shiplocate.domain.repository

import com.shiplocate.domain.model.TrackingStatus

/**
 * Интерфейс репозитория для управления GPS трекингом
 */
interface TrackingRepository {
    /**
     * Запускает GPS трекинг
     * @param loadId ID загрузки для трекинга
     */
    suspend fun startTracking(loadId: Long): Result<Unit>

    /**
     * Останавливает GPS трекинг
     */
    suspend fun stopTracking(): Result<Unit>

    /**
     * Получает текущий статус трекинга
     */
    suspend fun getTrackingStatus(): TrackingStatus
}
