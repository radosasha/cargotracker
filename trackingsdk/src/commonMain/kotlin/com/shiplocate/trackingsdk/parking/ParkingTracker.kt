package com.shiplocate.trackingsdk.parking

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import com.shiplocate.trackingsdk.parking.models.ParkingState
import com.shiplocate.trackingsdk.utils.LocationUtils
import com.shiplocate.trackingsdk.utils.models.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Clock

/**
 * Трекер парковки - определяет, находится ли пользователь в парковке
 * на основе анализа координат за последние 20 минут
 */
class ParkingTracker(
    private val logger: Logger,
    private val parkingRadiusMeters: Int, // Радиус парковки в метрах
    private val trackingTimeMinutes: Int, // Время отслеживания в минутах
) {

    private val coordinates = mutableListOf<ParkingLocation>()
    private val trackingTimeMs = trackingTimeMinutes * 60 * 1000L

    private val trackerFlow = MutableSharedFlow<ParkingState>()
    private var currentState = ParkingState.NOT_IN_PARKING

    companion object {
        private const val TAG = "ParkingTracker"
    }

    /**
     * Добавляет новую координату и проверяет, находится ли пользователь в парковке
     * @param parkingLocation объект с данными о местоположении
     * @return true если пользователь находится в парковке
     */
    suspend fun addCoordinate(parkingLocation: ParkingLocation): Boolean {

        if (currentState == ParkingState.IN_PARKING) {
            logger.debug(LogCategory.LOCATION, "$TAG: ignore adding coordinate, already in parking")
            return true
        }

        // Удаляем первую координату (старше 20 минут)
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (coordinates.isNotEmpty()) {
            val firstCoord = coordinates.first()
            if (currentTime - firstCoord.time > trackingTimeMs) {
                coordinates.removeAt(0)
            }
        }

        // Добавляем новую координату
        coordinates.add(parkingLocation)

        logger.debug(
            LogCategory.LOCATION,
            "$TAG: Added coordinate: lat=${parkingLocation.lat}, lon=${parkingLocation.lon}, error=${parkingLocation.error}, total=${coordinates.size}"
        )

        // Если координат недостаточно для анализа, считаем что не в парковке
        if (coordinates.size < 2) {
            logger.debug(LogCategory.LOCATION, "$TAG: Not enough coordinates for analysis (${coordinates.size})")
            return false
        }

        // Вычисляем центр координат
        val latLngs = coordinates.map {
            LatLng(it.lat, it.lon, it.error)
        }
        val center = LocationUtils.getGeographicCenter(latLngs)

        logger.debug(LogCategory.LOCATION, "$TAG: Center calculated: lat=${center.latitude}, lon=${center.longitude}")

        // Проверяем, все ли координаты находятся в радиусе парковки
        val allInParkingRadius = LocationUtils.areAllInRadius(center, latLngs, parkingRadiusMeters)

        logger.info(LogCategory.LOCATION, "$TAG: Parking status: $allInParkingRadius (${coordinates.size} coordinates)")
        currentState = if (allInParkingRadius) ParkingState.IN_PARKING else ParkingState.NOT_IN_PARKING
        trackerFlow.emit(currentState)
        return allInParkingRadius
    }

    /**
     * Очищает все сохраненные координаты
     */
    fun clear() {
        coordinates.clear()
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared all coordinates")
    }

    fun observeParkingStatus(): Flow<ParkingState> {
        return trackerFlow
    }
}
