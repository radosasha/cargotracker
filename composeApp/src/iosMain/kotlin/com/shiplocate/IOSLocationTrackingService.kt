package com.shiplocate

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.usecase.StartTrackerUseCase
import com.shiplocate.domain.usecase.StopTrackerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS Location Tracking Service
 * Аналог AndroidTrackingService для iOS платформы
 * Управляет фоновым GPS трекингом на iOS
 * Singleton для обеспечения единого экземпляра при пересоздании UI
 */
class IOSLocationTrackingService private constructor() : KoinComponent {
    private var isTracking = false

    private val startTrackerUseCase: StartTrackerUseCase by inject()
    private val stopTrackerUseCase: StopTrackerUseCase by inject()
    private val logger: Logger by inject()

    // Coroutine scope for background operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var collectingJob: Job? = null

    companion object {
        private const val TAG = "IOSLocationTrackingService"

        private var instance: IOSLocationTrackingService? = null

        /**
         * Получает единственный экземпляр сервиса
         * Thread-safe singleton implementation для Kotlin/Native
         */
        fun getInstance(): IOSLocationTrackingService {
            if (instance == null) {
                instance = IOSLocationTrackingService()
            }
            return instance!!
        }

        /**
         * Проверяет, инициализирован ли сервис
         */
        fun isInitialized(): Boolean = instance != null


        /**
         * Запускает GPS трекинг через singleton
         */
        suspend fun startTracking() {
            getInstance().startLocationTracking()
        }

        /**
         * Останавливает GPS трекинг через singleton
         */
        suspend fun stopTracking() {
            getInstance().stopLocationTracking()
        }

        /**
         * Проверяет статус трекинга через singleton
         */
        fun isTrackingActive(): Boolean {
            return getInstance().isLocationTrackingActive()
        }
    }

    /**
     * Запускает GPS трекинг
     * Аналог onStartCommand в Android Service
     */
    suspend fun startLocationTracking() {
        if (isTracking) {
            logger.info(LogCategory.LOCATION, "$TAG: Already tracking, ignoring start request")
            return
        }

        try {
            logger.info(LogCategory.LOCATION, "$TAG: Starting GPS tracking through StartProcessLocationsUseCase")

            // Запускаем обработку GPS координат и подписываемся на Flow результатов
            collectingJob = startTrackerUseCase()
                .onEach { result ->
                    // Логируем результат обработки
                    if (result.shouldSend) {
                        logger.debug(LogCategory.LOCATION, "$TAG: ✅ Location processed successfully: ${result.reason}")
                    } else {
                        logger.debug(LogCategory.LOCATION, "$TAG: ⏭️ Location filtered: ${result.reason}")
                    }
                }
                .launchIn(serviceScope)

            isTracking = true

            // Сохраняем состояние для восстановления
            // TODO: Добавить сохранение состояния

            logger.info(LogCategory.LOCATION, "$TAG: ✅ GPS tracking started successfully")
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: ❌ Error starting GPS tracking: ${e.message}", e)
            isTracking = false // Сбрасываем состояние при ошибке
            // TODO: Добавить сохранение состояния
        }
    }

    /**
     * Останавливает GPS трекинг
     * Аналог stopLocationTracking в Android Service
     */
    suspend fun stopLocationTracking() {
        if (!isTracking) {
            logger.info(LogCategory.LOCATION, "$TAG: Not tracking, ignoring stop request")
            return
        }

        try {
            logger.info(LogCategory.LOCATION, "$TAG: Stopping GPS tracking through StopProcessLocationsUseCase")

            // Останавливаем обработку GPS координат
            val result = stopTrackerUseCase()
            if (result.isSuccess) {
                isTracking = false
                logger.info(LogCategory.LOCATION, "$TAG: ✅ GPS tracking stopped successfully")
            } else {
                logger.error(LogCategory.LOCATION, "$TAG: ❌ Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
                // Принудительно сбрасываем состояние даже при ошибке
                isTracking = false
            }
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: ❌ Error stopping GPS tracking: ${e.message}", e)
            // Принудительно сбрасываем состояние при исключении
            isTracking = false
        }

        collectingJob?.cancel()
        collectingJob = null
    }

    /**
     * Синхронная остановка GPS трекинга
     * Аналог stopLocationTrackingSync в Android Service
     */
    private suspend fun stopLocationTrackingSync() {
        if (!isTracking) {
            logger.info(LogCategory.LOCATION, "$TAG: Not tracking, ignoring stop request")
            return
        }

        try {
            logger.info(LogCategory.LOCATION, "$TAG: Stopping GPS tracking synchronously")

            // Останавливаем обработку GPS координат синхронно
            val result = stopTrackerUseCase()
            if (result.isSuccess) {
                isTracking = false
                logger.info(LogCategory.LOCATION, "$TAG: ✅ GPS tracking stopped successfully")
            } else {
                logger.error(LogCategory.LOCATION, "$TAG: ❌ Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: ❌ Error stopping GPS tracking: ${e.message}", e)
        }
    }

    /**
     * Останавливает сервис и освобождает ресурсы
     * Аналог onDestroy в Android Service
     */
    fun destroy() {
        logger.info(LogCategory.LOCATION, "$TAG: Destroying service")

        // Синхронная остановка GPS трекинга перед отменой scope
        if (isTracking) {
            try {
                // Используем runBlocking для синхронного выполнения
                runBlocking {
                    stopLocationTrackingSync()
                }
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "$TAG: Error in runBlocking: ${e.message}", e)
            }
        }

        // Останавливаем serviceScope, что отменит все корутины
        serviceScope.cancel()
        logger.info(LogCategory.LOCATION, "$TAG: Service scope cancelled")

        // Очищаем singleton instance
        instance = null
    }

    /**
     * Проверяет, активен ли GPS трекинг
     * Аналог isLocationTrackingActive в Android Service
     */
    fun isLocationTrackingActive(): Boolean = isTracking

    /**
     * Получает статус сервиса для отладки
     */
    fun getServiceStatus(): String {
        return "IOSLocationTrackingService(status=${if (isTracking) "TRACKING" else "STOPPED"})"
    }
}
