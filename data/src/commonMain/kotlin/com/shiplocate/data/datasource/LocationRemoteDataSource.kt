package com.shiplocate.data.datasource

import com.shiplocate.data.model.LocationDataModel

/**
 * Remote Data Source для отправки GPS данных на сервер
 */
interface LocationRemoteDataSource {
    /**
     * Отправляет GPS координаты на сервер
     * @param token Bearer token для аутентификации
     * @param serverLoadId ID груза на сервере
     * @param locations список координат для отправки
     */
    suspend fun sendLocations(
        token: String,
        serverLoadId: Long,
        locations: List<LocationDataModel>,
    ): Result<Unit>
}
