package com.shiplocate.data.datasource

import com.shiplocate.data.model.LocationDataModel

/**
 * Remote Data Source для отправки GPS данных на сервер
 */
interface LocationRemoteDataSource {
    /**
     * Отправляет одну GPS координату на сервер
     */
    suspend fun sendLocation(
        loadId: String,
        location: LocationDataModel,
    ): Result<Unit>

    /**
     * Отправляет GPS координаты на сервер
     */
    suspend fun sendLocations(
        loadId: String,
        locations: List<LocationDataModel>,
    ): Result<Unit>
}
