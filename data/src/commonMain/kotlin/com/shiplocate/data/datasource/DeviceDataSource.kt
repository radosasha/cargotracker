package com.shiplocate.data.datasource

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

    /**
     * Получает информацию о платформе устройства
     * @return String - название платформы (Android/iOS)
     */
    suspend fun getPlatform(): String

    /**
     * Получает версию операционной системы
     * @return String - версия ОС
     */
    suspend fun getOsVersion(): String

    /**
     * Получает модель устройства
     * @return String - модель устройства
     */
    suspend fun getDeviceModel(): String
}
