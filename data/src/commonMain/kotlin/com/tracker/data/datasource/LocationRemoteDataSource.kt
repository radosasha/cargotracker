package com.tracker.data.datasource

import com.tracker.data.model.LocationDataModel

/**
 * Remote Data Source для отправки GPS данных на сервер
 */
interface LocationRemoteDataSource {
    
    /**
     * Отправляет одну GPS координату на сервер
     */
    suspend fun sendLocation(location: LocationDataModel): Result<Unit>
    
    /**
     * Отправляет GPS координаты на сервер
     */
    suspend fun sendLocations(locations: List<LocationDataModel>): Result<Unit>
    
    /**
     * Проверяет соединение с сервером
     */
    suspend fun checkConnection(): Result<Boolean>
}
