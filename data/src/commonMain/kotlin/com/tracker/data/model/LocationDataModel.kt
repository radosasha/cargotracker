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
    val timestamp: Instant,
    val isValid: Boolean = true,
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val course: Float? = null, // bearing переименован в course для совместимости
    val batteryLevel: Int? = null,
    val deviceId: String? = null
)

