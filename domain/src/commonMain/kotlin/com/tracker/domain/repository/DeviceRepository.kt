package com.tracker.domain.repository

/**
 * Репозиторий для получения информации об устройстве
 * Абстракция над data слоем для работы с устройством
 */
interface DeviceRepository {
    /**
     * Получает текущий уровень батареи
     * @return Float? - уровень батареи в процентах (0.0-100.0) или null если недоступно
     */
    suspend fun getBatteryLevel(): Float?

    /**
     * Проверяет, подключено ли зарядное устройство
     * @return Boolean - true если устройство заряжается
     */
    suspend fun isCharging(): Boolean
}
