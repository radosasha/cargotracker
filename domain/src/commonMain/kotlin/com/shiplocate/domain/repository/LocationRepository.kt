package com.shiplocate.domain.repository

import com.shiplocate.domain.model.DeviceLocation
import com.shiplocate.domain.model.GpsLocation

/**
 * Repository для работы с GPS координатами
 */
interface LocationRepository {
    /**
     * Отправляет GPS координаты на сервер
     * @param token Bearer token для аутентификации
     * @param serverLoadId ID груза на сервере
     * @param locations список координат для отправки
     */
    suspend fun sendLocations(
        token: String,
        serverLoadId: Long,
        locations: List<DeviceLocation>,
    ): Result<Unit>

    /**
     * Сохраняет координату в локальную БД
     * @return ID сохраненной записи
     */
    suspend fun saveLocationToDb(
        location: GpsLocation,
        batteryLevel: Float? = null,
    ): Long

    /**
     * Получает все неотправленные координаты из БД
     * @return Список пар (ID в БД, Location)
     */
    suspend fun getUnsentDeviceLocations(): List<Pair<Long, DeviceLocation>>

    /**
     * Удаляет координату из БД по ID
     */
    suspend fun deleteLocationFromDb(id: Long)

    /**
     * Удаляет координаты из БД по списку ID
     */
    suspend fun deleteLocationsFromDb(ids: List<Long>)

    /**
     * Получает последнюю сохраненную координату из БД
     */
    suspend fun getLastSavedLocation(): GpsLocation?

    /**
     * Получает количество неотправленных координат
     */
    suspend fun getUnsentCount(): Int
}
