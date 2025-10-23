package com.shiplocate.data.model

import kotlinx.datetime.Instant

/**
 * Модель GPS координаты в Data слое
 * Содержит сырые данные от GPS системы
 */
data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double?,
    val speed: Float?,
    val bearing: Float?,
    val timestamp: Instant,
    val provider: String,
)
