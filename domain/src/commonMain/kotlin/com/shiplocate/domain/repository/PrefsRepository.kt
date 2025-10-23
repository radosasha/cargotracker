package com.shiplocate.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с настройками приложения
 * Domain слой - абстракция над DataStore
 */
interface PrefsRepository {
    /**
     * Сохраняет состояние трекинга
     */
    suspend fun saveTrackingState(isTracking: Boolean)

    /**
     * Получает состояние трекинга
     */
    suspend fun getTrackingState(): Boolean?

    /**
     * Получает поток состояния трекинга
     */
    fun getTrackingStateFlow(): Flow<Boolean?>

    /**
     * Сохраняет настройки точности GPS
     */
    suspend fun saveGpsAccuracy(accuracy: String)

    /**
     * Получает настройки точности GPS
     */
    suspend fun getGpsAccuracy(): String?

    /**
     * Получает поток настроек точности GPS
     */
    fun getGpsAccuracyFlow(): Flow<String?>

    /**
     * Сохраняет интервал обновления GPS
     */
    suspend fun saveGpsInterval(interval: Int)

    /**
     * Получает интервал обновления GPS
     */
    suspend fun getGpsInterval(): Int?

    /**
     * Получает поток интервала обновления GPS
     */
    fun getGpsIntervalFlow(): Flow<Int?>

    /**
     * Сохраняет фильтр расстояния
     */
    suspend fun saveDistanceFilter(distance: Int)

    /**
     * Получает фильтр расстояния
     */
    suspend fun getDistanceFilter(): Int?

    /**
     * Получает поток фильтра расстояния
     */
    fun getDistanceFilterFlow(): Flow<Int?>

    /**
     * Очищает все настройки
     */
    suspend fun clearAllSettings()

    /**
     * Сохраняет номер телефона пользователя
     */
    suspend fun savePhoneNumber(phoneNumber: String)

    /**
     * Получает номер телефона пользователя
     */
    suspend fun getPhoneNumber(): String?
}
