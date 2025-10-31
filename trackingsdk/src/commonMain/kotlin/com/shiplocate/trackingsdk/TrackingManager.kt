package com.shiplocate.trackingsdk

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationSyncService
import com.shiplocate.trackingsdk.motion.MotionTracker
import com.shiplocate.trackingsdk.motion.models.MotionTrackerEvent
import com.shiplocate.trackingsdk.parking.ParkingTracker
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TrackingManager(
    private val tripRecorder: TripRecorder,
    private val locationSyncService: LocationSyncService,
    private val parkingTracker: ParkingTracker,
    private val motionTracker: MotionTracker,
    private val logger: Logger,
    private val scope: CoroutineScope,
) {
    private var currentState = TrackingState.TRIP_RECORDING
    private val trackingState = MutableSharedFlow<TrackingStateEvent>(replay = 0)

    private var parkingStatusJob: Job? = null
    private var tripRecorderJob: Job? = null
    private var motionTrackerJob: Job? = null

    fun startTracking(): Flow<TrackingStateEvent> {
        scope.launch {
            locationSyncService.startSync()
            switchToState(currentState)
        }
        return trackingState
    }

    private suspend fun switchToState(state: TrackingState) {

        // Проверяем, не переключаемся ли в то же состояние
        if (currentState == state) {
            logger.debug(LogCategory.LOCATION, "TrackingManager: Already in state $state, skipping switch")
            return
        }

        when (state) {
            TrackingState.IN_PARKING -> {
                // Отменяем все jobs связанные с TRIP_RECORDING
                tripRecorderJob?.cancel()
                parkingStatusJob?.cancel()
                tripRecorder.stopTracking()

                // Отменяем предыдущий motionTrackingJob, если он существует
                motionTrackerJob?.cancel()

                // Запускаем MotionTracker ПЕРЕД подпиской на события
                motionTracker.startTracking()

                // Подписываемся на события движения в транспорте ПОСЛЕ запуска трекера
                motionTrackerJob = motionTracker.observeMotionTrigger().onEach { event ->
                    when (event) {
                        is MotionTrackerEvent.InVehicle -> {
                            // Обнаружено движение в транспорте - переключаемся в TRIP_RECORDING
                            logger.info(LogCategory.LOCATION, "TrackingManager: Vehicle motion detected, switching to TRIP_RECORDING")
                            switchToState(TrackingState.TRIP_RECORDING)
                        }
                        is MotionTrackerEvent.CheckingMotion -> {
                            // Результаты анализа движения - передаем в trackingState
                            trackingState.emit(
                                TrackingStateEvent.MotionAnalysis(
                                    analysisResult = event.statistics,
                                    timestamp = event.timestamp,
                                )
                            )
                        }
                    }
                }.launchIn(scope)

                currentState = TrackingState.IN_PARKING
                logger.info(LogCategory.LOCATION, "TrackingManager: Switched to IN_PARKING state")
            }

            TrackingState.TRIP_RECORDING -> {
                // Сначала останавливаем MotionTracker, потом отменяем job
                motionTracker.stopTracking()
                motionTrackerJob?.cancel()
                motionTrackerJob = null

                // Запускаем TripRecorder и подписываемся на координаты
                tripRecorderJob = tripRecorder.startTracking().onEach { result ->
                    addToParkingTracker(result)
                    trackingState.emit(TrackingStateEvent.LocationProcessed(result = result))
                }.launchIn(scope)

                // Подписываемся на статус парковки
                parkingStatusJob = parkingTracker.observeParkingStatus().onEach {
                    logger.info(LogCategory.LOCATION, "TrackingManager: User entered parking, stopping tracking, reason: $it")
                    // Вызываем switchToState через scope для избежания рекурсии
                    switchToState(TrackingState.IN_PARKING)
                }.launchIn(scope)

                currentState = TrackingState.TRIP_RECORDING
                logger.info(LogCategory.LOCATION, "TrackingManager: Switched to TRIP_RECORDING state")
            }
        }

    }


    suspend fun addToParkingTracker(location: LocationProcessResult) {
        if (currentState == TrackingState.TRIP_RECORDING) {
            // Передаем координаты в ParkingTracker для анализа парковки
            val parkingLocation = ParkingLocation(
                location.lastCoordinateLat,
                location.lastCoordinateLon,
                location.lastCoordinateTime,
                location.coordinateErrorMeters
            )
            val isInParking = parkingTracker.addCoordinate(parkingLocation)
            logger.debug(LogCategory.LOCATION, "TrackingManager: Parking status: $isInParking")
        } else {
            logger.debug(LogCategory.LOCATION, "TrackingManager: Ignore adding coordinate, Parking status: ${TrackingState.IN_PARKING}")
        }
    }

    suspend fun stopTracking(): Result<Unit> {
        // Отменяем все jobs перед остановкой сервисов
        parkingStatusJob?.cancel()
        tripRecorderJob?.cancel()
        motionTrackerJob?.cancel()

        // Останавливаем сервисы
        locationSyncService.stopSync()
        parkingTracker.clear()
        motionTracker.destroy()

        // Очищаем ссылки на jobs
        parkingStatusJob = null
        tripRecorderJob = null
        motionTrackerJob = null

        return tripRecorder.stopTracking()
    }
}
