package com.shiplocate.trackingsdk

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

class IOSTrackingService(
    val trackingManager: TrackingManager,
    val logger: Logger,
) {

    private var isTracking = false

    // Coroutine scope for background operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var collectingJob: Job? = null

    companion object {
        private const val TAG = "TrackingSDKIOS"
    }

    suspend fun startTracking(loadId: Long): Result<Unit> {
        return try {
            if (isTracking) {
                logger.info(LogCategory.LOCATION, "$TAG: Already tracking, ignoring start request")
                return Result.success(Unit)
            }

            logger.info(LogCategory.LOCATION, "$TAG: Starting GPS tracking through StartProcessLocationsUseCase")

            // Запускаем обработку GPS координат и подписываемся на Flow результатов
            collectingJob = trackingManager.startTracking(loadId)
                .onEach { event ->
                    when (event) {
                        is TrackingStateEvent.LocationProcessed -> {
                            // Логируем результат обработки
                            if (event.result.shouldSend) {
                                logger.debug(LogCategory.LOCATION, "$TAG: ✅ Location processed successfully: ${event.result.reason}")
                            } else {
                                logger.debug(LogCategory.LOCATION, "$TAG: ⏭️ Location filtered: ${event.result.reason}")
                            }
                        }

                        is TrackingStateEvent.MotionAnalysis -> {

                        }
                    }
                }
                .launchIn(serviceScope)

            isTracking = true

            logger.info(LogCategory.LOCATION, "$TAG: ✅ GPS tracking started successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: ❌ Error starting GPS tracking: ${e.message}", e)
            isTracking = false
            Result.failure(e)
        }
    }

    suspend fun stopTracking(): Result<Unit> {
        return try {
            if (!isTracking) {
                logger.info(LogCategory.LOCATION, "$TAG: Not tracking, ignoring stop request")
                return Result.success(Unit)
            }

            logger.info(LogCategory.LOCATION, "$TAG: Stopping GPS tracking through StopProcessLocationsUseCase")

            // Останавливаем обработку GPS координат
            val result = trackingManager.stopTracking()
            if (result.isSuccess) {
                isTracking = false
                logger.info(LogCategory.LOCATION, "$TAG: ✅ GPS tracking stopped successfully")
            } else {
                logger.error(LogCategory.LOCATION, "$TAG: ❌ Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
                isTracking = false
            }

            collectingJob?.cancel()
            collectingJob = null

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: ❌ Error stopping GPS tracking: ${e.message}", e)
            isTracking = false
            Result.failure(e)
        }
    }

    fun destroy() {
        logger.info(LogCategory.LOCATION, "$TAG: Destroying SDK")

        // Синхронная остановка GPS трекинга перед отменой scope
        if (isTracking) {
            try {
                runBlocking {
                    stopTracking()
                }
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "$TAG: Error in runBlocking: ${e.message}", e)
            }
        }

        // Останавливаем serviceScope, что отменит все корутины
        serviceScope.cancel()
        logger.info(LogCategory.LOCATION, "$TAG: Service scope cancelled")
    }
}
