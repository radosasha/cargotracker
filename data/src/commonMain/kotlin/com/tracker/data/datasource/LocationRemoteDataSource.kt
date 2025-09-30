package com.tracker.data.datasource

import com.tracker.data.model.LocationDataModel

/**
 * Remote Data Source для отправки GPS данных на сервер
 */
interface LocationRemoteDataSource {
    
    /**
     * Отправляет GPS координаты на сервер
     */
    suspend fun sendLocations(locations: List<LocationDataModel>): Result<Unit>
    
    /**
     * Проверяет соединение с сервером
     */
    suspend fun checkConnection(): Result<Boolean>
}
