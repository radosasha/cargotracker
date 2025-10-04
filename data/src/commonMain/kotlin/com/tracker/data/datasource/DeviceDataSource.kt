package com.tracker.data.datasource

/**
 * Интерфейс для получения информации об устройстве
 * Абстракция над платформо-специфичными реализациями
 */
interface DeviceDataSource {
    
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
