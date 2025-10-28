package com.shiplocate.trackingsdk

/**
 * Класс для представления географических координат
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double,
    val error: Int = 0 // Погрешность в метрах
)
