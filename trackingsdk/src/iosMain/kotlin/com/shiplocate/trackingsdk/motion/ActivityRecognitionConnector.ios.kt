package com.shiplocate.trackingsdk.motion

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.CoreMotion.CMMotionActivity
import platform.CoreMotion.CMMotionActivityManager
import platform.Foundation.NSOperationQueue
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS actual реализация ActivityRecognitionConnector
 */
actual class ActivityRecognitionConnector(
    private val activityFrequencyMs: Long,
    private val logger: Logger,
    private val scope: CoroutineScope,
) {

    private var isTracking = false
    
    // Motion manager для отслеживания активности
    private val motionManager: CMMotionActivityManager = CMMotionActivityManager()

    // Flow для отправки событий движения
    private val motionEvents = MutableSharedFlow<MotionEvent>(replay = 0)

    companion object {
        private val TAG = ActivityRecognitionConnector::class.simpleName
    }

    actual fun startTracking() {
        if (isTracking) {
            logger.debug(LogCategory.LOCATION, "$TAG: Already tracking motion")
            return
        }

        isTracking = true
        logger.info(LogCategory.LOCATION, "$TAG: Starting ActivityRecognition (iOS implementation)")

        dispatch_async(dispatch_get_main_queue()) {
            try {
                // Запускаем отслеживание активности через CMMotionActivityManager
                motionManager.startActivityUpdatesToQueue(
                    queue = NSOperationQueue.mainQueue
                ) { activity ->
                    if (activity != null && isTracking) {
                        // Конвертируем CMMotionActivity в MotionEvent
                        val motionState = convertActivityToMotionState(activity)
                        val confidence = calculateConfidence(activity)

                        logger.debug(
                            LogCategory.LOCATION,
                            "$TAG: Activity recognition: $motionState (confidence: $confidence%)"
                        )

                        val motionEvent = MotionEvent(
                            motionState = motionState,
                            confidence = confidence,
                            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                        )
                        
                        // Отправляем событие в flow
                        motionEvents.tryEmit(motionEvent)
                    }
                }
                
                logger.info(LogCategory.LOCATION, "$TAG: ActivityRecognition started successfully")
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "$TAG: Error starting ActivityRecognition: ${e.message}", e)
                isTracking = false
            }
        }
    }

    actual fun stopTracking() {
        if (!isTracking) {
            logger.debug(LogCategory.LOCATION, "$TAG: Not tracking motion")
            return
        }

        isTracking = false
        logger.info(LogCategory.LOCATION, "$TAG: Stopping ActivityRecognition")

        dispatch_async(dispatch_get_main_queue()) {
            try {
                motionManager.stopActivityUpdates()
                logger.info(LogCategory.LOCATION, "$TAG: Successfully stopped ActivityRecognition")
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "$TAG: Error stopping ActivityRecognition: ${e.message}", e)
            }
        }
    }

    actual fun clear() {
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared")
        // Очистка не нужна для ActivityRecognition на iOS
    }

    actual fun destroy() {
        stopTracking()
        logger.debug(LogCategory.LOCATION, "$TAG: Destroyed")
    }

    actual fun observeMotionEvents(): Flow<MotionEvent> = motionEvents

    /**
     * Конвертирует CMMotionActivity в MotionState
     * Приоритет состояний: IN_VEHICLE > ON_BICYCLE > RUNNING > WALKING > STATIONARY > UNKNOWN
     */
    private fun convertActivityToMotionState(activity: CMMotionActivity): MotionState {
        return when {
            activity.automotive == true -> MotionState.IN_VEHICLE
            activity.cycling == true -> MotionState.ON_BICYCLE
            activity.running == true -> MotionState.RUNNING
            activity.walking == true -> MotionState.WALKING
            activity.stationary == true -> MotionState.STATIONARY
            else -> MotionState.UNKNOWN
        }
    }

    /**
     * Вычисляет уверенность на основе CMMotionActivity
     * CMMotionActivity имеет confidence в диапазоне 0.0-1.0, конвертируем в проценты
     */
    private fun calculateConfidence(activity: CMMotionActivity): Int {
        // Используем общую уверенность из CMMotionActivity (диапазон 0.0-1.0)
        val confidence = activity.confidence
        
        // Конвертируем из диапазона 0.0-1.0 в проценты 0-100
        return (confidence * 100).toInt().coerceIn(0, 100)
    }
}
