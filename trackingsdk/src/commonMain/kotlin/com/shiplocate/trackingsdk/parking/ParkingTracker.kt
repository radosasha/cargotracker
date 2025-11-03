package com.shiplocate.trackingsdk.parking

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.parking.models.InReason
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import com.shiplocate.trackingsdk.parking.models.ParkingStatus
import com.shiplocate.trackingsdk.utils.LocationUtils
import com.shiplocate.trackingsdk.utils.ParkingTimeoutTimer
import com.shiplocate.trackingsdk.utils.models.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private val scope: CoroutineScope,
) {

    private val locationsHistory = mutableListOf<ParkingLocation>()

    private val parkingStateFlow = MutableSharedFlow<ParkingStatus>(replay = 0)

    init {
        // Подписываемся на события таймера
        // TODO пока что отключаем парковку
//        parkingTimeoutTimer.timerEvent
//            .onEach { onTimerEvent() }
//            .launchIn(scope)
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

        // Добавляем новую координату
        locationsHistory.add(parkingLocation)

        // Удаляем  старше окна анализа (относительно времени последней точки)
        val lastTime = locationsHistory.last().time
        while (locationsHistory.size > 1 && (lastTime - locationsHistory.first().time) > triggerTimeMs) {
            locationsHistory.removeAt(0)
        }

        logger.debug(
            LogCategory.LOCATION,
            "$TAG: Added coordinate: lat=${parkingLocation.lat}, lon=${parkingLocation.lon}, error=${parkingLocation.error}, total=${locationsHistory.size}"
        )

        // Если координат недостаточно для анализа, считаем что не в парковке
        if (locationsHistory.size < 2) {
            logger.debug(LogCategory.LOCATION, "$TAG: Not enough coordinates for analysis (${locationsHistory.size})")
            return false
        }

        // Проверяем, достаточно ли времени прошло для анализа парковки
        val firstCoord = locationsHistory.first()
        val lastCoord = locationsHistory.last()
        val timeSpan = lastCoord.time - firstCoord.time

        if (timeSpan < triggerTimeMs) {
            return false
        }

        // Вычисляем центр координат
        val latLngs = locationsHistory.map {
            LatLng(it.lat, it.lon, it.error)
        }
        val center = LocationUtils.getGeographicCenter(latLngs)

        // Проверяем, все ли координаты находятся в радиусе парковки
        val allInParkingRadius = LocationUtils.areAllInRadius(center, latLngs, parkingRadiusMeters)

        logger.info(
            LogCategory.LOCATION,
            "$TAG: Parking status: all coords in parking radius($allInParkingRadius) (${locationsHistory.size} coordinates)"
        )
        if (allInParkingRadius) {
            val newStatus = ParkingStatus.InParking(InReason.Radius)
            parkingStateFlow.emit(newStatus)
            // После входа в парковку очищаем, чтобы новая детекция началась с нуля
            clear()
            logger.debug(LogCategory.LOCATION, "$TAG: Cleared history after InParking (Radius) event")
        }
        return allInParkingRadius
    }


    /**
     * Обрабатывает событие таймера
     */
    private suspend fun onTimerEvent() {
        val currentTime = Clock.System.now().toEpochMilliseconds()

        if (locationsHistory.isEmpty()) {
            // Нет координат — таймер не должен был вообще срабатывать
            return
        }

        val lastCoordinate = locationsHistory.last()
        val timeDifference = currentTime - lastCoordinate.time

        if (timeDifference > parkingTimeoutTimer.timeoutMs) {
            logger.debug(LogCategory.LOCATION, "$TAG: Last coordinate too old (${timeDifference}ms), keep-alive parking by timeout")
            val newStatus = ParkingStatus.InParking(InReason.Timeout)
            parkingStateFlow.emit(newStatus)
            // Очистка состояния; таймер не перезапускаем
            clear()
            logger.debug(LogCategory.LOCATION, "$TAG: Cleared history after InParking (Timeout) event")
        } else {
            // Перезапускаем таймер на оставшееся время + 1 минута
            val remainingTimeMs =
                (parkingTimeoutTimer.timeoutMs - timeDifference + (60 * 1000L)).coerceAtLeast(60 * 1000L) // минимум 1 минута
            logger.debug(LogCategory.LOCATION, "$TAG: Restarting timer for ${remainingTimeMs}ms")
            parkingTimeoutTimer.start(remainingTimeMs)
        }
    }

    fun clear() {
        locationsHistory.clear()
        parkingTimeoutTimer.stop()
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared all coordinates")
    }

    fun observeParkingStatus(): Flow<ParkingStatus> {
        return parkingStateFlow
    }
}
