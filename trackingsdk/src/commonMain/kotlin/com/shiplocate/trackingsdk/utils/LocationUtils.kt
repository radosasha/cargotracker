package com.shiplocate.trackingsdk.utils

import com.shiplocate.trackingsdk.LatLng
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Утилиты для работы с географическими координатами
 */
object LocationUtils {

    /**
     * Вычисляет географический центр списка координат
     * Использует сферическую геометрию для точного вычисления центра
     */
    fun getGeographicCenter(coords: List<LatLng>): LatLng {
        if (coords.isEmpty()) throw IllegalStateException("coordinates list is empty")

        var x = 0.0
        var y = 0.0
        var z = 0.0

        for (coord in coords) {
            val latRad = PI * coord.latitude / 180.0
            val lonRad = PI * coord.longitude / 180.0

            x += cos(latRad) * cos(lonRad)
            y += cos(latRad) * sin(lonRad)
            z += sin(latRad)
        }

        val total = coords.size.toDouble()
        x /= total
        y /= total
        z /= total

        val lon = atan2(y, x)
        val hyp = sqrt(x * x + y * y)
        val lat = atan2(z, hyp)

        // Вычисляем среднюю погрешность
        val avgError = coords.map { it.error }.average().toInt()

        return LatLng(180.0 * lat / PI, 180.0 * lon / PI, avgError)
    }

    /**
     * Вычисляет расстояние между двумя координатами в метрах (формула Haversine)
     * Учитывает сферическую форму Земли для точного вычисления расстояния
     */
    fun calculateDistance(coord1: LatLng, coord2: LatLng): Double {
        val earthRadius = 6371000.0 // Радиус Земли в метрах

        val lat1Rad = PI * coord1.latitude / 180.0
        val lat2Rad = PI * coord2.latitude / 180.0
        val deltaLatRad = PI * (coord2.latitude - coord1.latitude) / 180.0
        val deltaLonRad = PI * (coord2.longitude - coord1.longitude) / 180.0

        val a = sin(deltaLatRad / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(deltaLonRad / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Проверяет, находится ли координата в заданном радиусе от центральной точки
     * с учетом погрешности обеих координат
     */
    fun isInRadius(
        centerCoord: LatLng,
        coordinate: LatLng,
        radiusMeters: Int,
    ): Boolean {
        val distance = calculateDistance(centerCoord, coordinate)
        val totalError = centerCoord.error + coordinate.error
        return distance - totalError < radiusMeters
    }

    /**
     * Проверяет, находятся ли все координаты в заданном радиусе от центральной точки
     */
    fun areAllInRadius(
        centerCoord: LatLng,
        coordinates: List<LatLng>,
        radiusMeters: Int,
    ): Boolean {
        return coordinates.all { coord ->
            isInRadius(centerCoord, coord, radiusMeters)
        }
    }
}
