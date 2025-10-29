package com.shiplocate.trackingsdk.parking

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import com.shiplocate.trackingsdk.parking.models.ParkingState
import com.shiplocate.trackingsdk.utils.LocationUtils
import com.shiplocate.trackingsdk.utils.ParkingTimeoutTimer
import com.shiplocate.trackingsdk.utils.models.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Трекер парковки - определяет, находится ли пользователь в парковке
 * на основе анализа координат за последние 20 минут
 */
class ParkingTracker(
    private val parkingTimeoutTimer: ParkingTimeoutTimer,
    // Радиус парковки в метрах
    private val parkingRadiusMeters: Int,
    // триггер парковки
    private val triggerTimeMs: Long = 20 * 60 * 1000L,
    private val logger: Logger,
) {

    private val coordinates = mutableListOf<ParkingLocation>()

    private val parkingStateFlow = MutableSharedFlow<ParkingState>(replay = 0)
    private var currentState = ParkingState.NOT_IN_PARKING

    // Таймер для отслеживания парковки
    private val scope = CoroutineScope(Dispatchers.Default)

    // Flow для уведомления о завершении парковки
    private val parkingTimeoutEvent = MutableSharedFlow<Unit>(replay = 0)

    init {
        // Подписываемся на события таймера
        scope.launch {
            parkingTimeoutTimer.timerEvent
                .onEach { onTimerEvent() }
                .launchIn(scope)
        }
    }

    companion object {
        private val TAG = ParkingTracker::class.simpleName
    }

    /**
     * Добавляет новую координату и проверяет, находится ли пользователь в парковке
     * @param parkingLocation объект с данными о местоположении
     * @return true если пользователь находится в парковке
     */
    suspend fun addCoordinate(parkingLocation: ParkingLocation): Boolean {
        // Проверяем, запущен ли таймер, если нет - запускаем
        if (!parkingTimeoutTimer.isRunning()) {
            logger.debug(LogCategory.LOCATION, "$TAG: Timer not running, starting it")
            parkingTimeoutTimer.start(parkingTimeoutTimer.timeoutMs)
        }

        if (currentState == ParkingState.IN_PARKING) {
            logger.debug(LogCategory.LOCATION, "$TAG: ignore adding coordinate, already in parking")
            return true
        }

        // Удаляем первую координату (старше 20 минут)
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (coordinates.isNotEmpty()) {
            val firstCoord = coordinates.first()
            if (currentTime - firstCoord.time > triggerTimeMs) {
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
        parkingStateFlow.emit(currentState)
        return allInParkingRadius
    }


    /**
     * Обрабатывает событие таймера
     */
    private suspend fun onTimerEvent() {
        val currentTime = Clock.System.now().toEpochMilliseconds()

        if (coordinates.isEmpty()) {
            logger.debug(LogCategory.LOCATION, "$TAG: No coordinates, sending parking finished event")
            parkingTimeoutEvent.emit(Unit)
            return
        }

        val lastCoordinate = coordinates.last()
        val timeDifference = currentTime - lastCoordinate.time

        if (timeDifference > parkingTimeoutTimer.timeoutMs) {
            logger.debug(LogCategory.LOCATION, "$TAG: Last coordinate too old (${timeDifference}ms), sending parking finished event")
            clear()
            parkingTimeoutEvent.emit(Unit)
        } else {
            // Перезапускаем таймер на оставшееся время + 1 минута
            val remainingTime = parkingTimeoutTimer.timeoutMs - timeDifference + (60 * 1000L) // +1 минута
            logger.debug(LogCategory.LOCATION, "$TAG: Restarting timer for ${remainingTime}ms")
            parkingTimeoutTimer.start(remainingTime)
        }
    }

    fun clear() {
        coordinates.clear()
        parkingTimeoutTimer.stop()
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared all coordinates")
    }

    fun observeParkingStatus(): Flow<ParkingState> {
        return parkingStateFlow
    }

    fun observeParkingTimeout(): Flow<Unit> {
        return parkingTimeoutEvent
    }
}
