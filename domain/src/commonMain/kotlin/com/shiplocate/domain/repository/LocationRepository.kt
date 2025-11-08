package com.shiplocate.domain.repository

import com.shiplocate.domain.model.DeviceLocation
import com.shiplocate.domain.model.GpsLocation

/**
 * Repository для работы с GPS координатами
 */
interface LocationRepository {
    /**
     * Сохраняет GPS координату (отправляет на сервер)
     */
    suspend fun sendLocation(
        serverLoadId: Long,
        location: GpsLocation,
    ): Result<Unit>

    /**
     * Отправляет несколько GPS координат на сервер
     */
    suspend fun sendLocations(
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
