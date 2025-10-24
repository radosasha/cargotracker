package com.shiplocate.domain.repository

import com.shiplocate.domain.model.Location

/**
 * Repository для работы с GPS координатами
 */
interface LocationRepository {
    /**
     * Сохраняет GPS координату (отправляет на сервер)
     */
    suspend fun sendLocation(
        loadId: String,
        location: Location,
    ): Result<Unit>

    /**
     * Отправляет несколько GPS координат на сервер
     */
    suspend fun sendLocations(
        loadId: String,
        locations: List<Location>,
    ): Result<Unit>

    /**
     * Сохраняет координату в локальную БД
     * @return ID сохраненной записи
     */
    suspend fun saveLocationToDb(
        location: Location,
        batteryLevel: Float? = null,
    ): Long

    /**
     * Получает все неотправленные координаты из БД
     * @return Список пар (ID в БД, Location)
     */
    suspend fun getUnsentLocations(loadId: String): List<Pair<Long, Location>>

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
    suspend fun getLastSavedLocation(loadId: String): Location?

    /**
     * Получает количество неотправленных координат
     */
    suspend fun getUnsentCount(): Int
}
