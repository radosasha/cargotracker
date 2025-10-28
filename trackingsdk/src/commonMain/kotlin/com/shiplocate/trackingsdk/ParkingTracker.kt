package com.shiplocate.trackingsdk

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import com.shiplocate.trackingsdk.utils.LocationUtils
import kotlin.math.*
import kotlinx.datetime.Clock

/**
 * Трекер парковки - определяет, находится ли пользователь в парковке
 * на основе анализа координат за последние 20 минут
 */
class ParkingTracker(
    private val logger: Logger,
    private val parkingRadiusMeters: Int = 200, // Радиус парковки в метрах
    private val trackingTimeMinutes: Int = 20, // Время отслеживания в минутах
) {

    private val coordinates = mutableListOf<LatLng>()
    private val trackingTimeMs = trackingTimeMinutes * 60 * 1000L

    companion object {
        private const val TAG = "ParkingTracker"
    }

    /**
     * Добавляет новую координату и проверяет, находится ли пользователь в парковке
     * @param parkingLocation объект с данными о местоположении
     * @return true если пользователь находится в парковке
     */
    fun addCoordinate(parkingLocation: ParkingLocation): Boolean {
        // Добавляем новую координату
        val newCoordinate = LatLng(parkingLocation.lat, parkingLocation.lon, parkingLocation.error)
        coordinates.add(newCoordinate)

        // Удаляем старые координаты (старше 20 минут)
        val currentTime = Clock.System.now().toEpochMilliseconds()
        while (coordinates.isNotEmpty()) {
            val firstCoordTime = parkingLocation.time - (coordinates.size - 1) * 1000L // Примерное время первой координаты
            if (currentTime - firstCoordTime > trackingTimeMs) {
                coordinates.removeAt(0) // Удаляем первую (самую старую) координату
            } else {
                break // Если первая координата еще актуальна, остальные тоже актуальны
            }
        }

        logger.debug(LogCategory.LOCATION, "$TAG: Added coordinate: lat=${parkingLocation.lat}, lon=${parkingLocation.lon}, error=${parkingLocation.error}, total=${coordinates.size}")

        // Если координат недостаточно для анализа, считаем что не в парковке
        if (coordinates.size < 3) {
            logger.debug(LogCategory.LOCATION, "$TAG: Not enough coordinates for analysis (${coordinates.size})")
            return false
        }

        // Вычисляем центр координат
        val center = LocationUtils.getGeographicCenter(coordinates)
        if (center == null) {
            logger.debug(LogCategory.LOCATION, "$TAG: Could not calculate center")
            return false
        }

        logger.debug(LogCategory.LOCATION, "$TAG: Center calculated: lat=${center.latitude}, lon=${center.longitude}")

        // Проверяем, все ли координаты находятся в радиусе парковки
        val allInParkingRadius = LocationUtils.areAllInRadius(center, coordinates, parkingRadiusMeters)

        logger.info(LogCategory.LOCATION, "$TAG: Parking status: $allInParkingRadius (${coordinates.size} coordinates)")
        return allInParkingRadius
    }

    /**
     * Очищает все сохраненные координаты
     */
    fun clear() {
        coordinates.clear()
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared all coordinates")
    }

    /**
     * Возвращает количество сохраненных координат
     */
    fun getCoordinateCount(): Int = coordinates.size

    /**
     * Возвращает последнюю координату
     */
    fun getLastCoordinate(): LatLng? = coordinates.lastOrNull()
}
