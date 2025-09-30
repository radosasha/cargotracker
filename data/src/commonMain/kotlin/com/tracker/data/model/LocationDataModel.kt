package com.tracker.data.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

/**
 * Data модель для GPS координат
 * Используется для сериализации/десериализации и работы с API
 */
@Serializable
data class LocationDataModel(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Instant,
    val deviceId: String? = null
)

/**
 * Data модель для запроса отправки координат на сервер
 */
@Serializable
data class LocationRequestDataModel(
    val locations: List<LocationDataModel>
)

/**
 * Data модель ответа сервера
 */
@Serializable
data class LocationResponseDataModel(
    val success: Boolean,
    val message: String? = null,
    val processedCount: Int = 0
)
