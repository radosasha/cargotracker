package com.shiplocate.data.datasource

import com.shiplocate.data.model.GpsLocation
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для управления GPS трекингом
 * Абстракция над платформо-специфичными реализациями
 */
interface GpsManager {
    /**
     * Запускает GPS трекинг
     * @return Result<Unit> - результат операции
     */
    suspend fun startGpsTracking(): Flow<GpsLocation>

    /**
     * Останавливает GPS трекинг
     * @return Result<Unit> - результат операции
     */
    suspend fun stopGpsTracking(): Result<Unit>

    /**
     * Проверяет, активен ли GPS трекинг
     * @return Boolean - true если трекинг активен
     */
    fun isGpsTrackingActive(): Boolean
}
