package com.shiplocate.trackingsdk.motion

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * iOS actual реализация ActivityRecognitionConnector
 */
actual class ActivityRecognitionConnector(
    private val logger: Logger,
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var isTracking = false
    
    // Flow для отправки событий движения
    private val _motionEvents = MutableSharedFlow<MotionEvent>(replay = 0)
    actual val motionEvents: SharedFlow<MotionEvent> = _motionEvents.asSharedFlow()
    
    companion object {
        private val TAG = ActivityRecognitionConnector::class.simpleName
    }

    actual fun startTracking() {
        if (isTracking) {
            logger.debug(LogCategory.LOCATION, "$TAG: Already tracking motion")
            return
        }
        
        isTracking = true
        logger.info(LogCategory.LOCATION, "$TAG: Started motion tracking (iOS implementation)")
        
        // Простая симуляция - отправляем разные состояния движения
        scope.launch {
            while (isTracking) {
                val states = listOf(
                    MotionState.IN_VEHICLE to 90,
                    MotionState.WALKING to 85,
                    MotionState.STATIONARY to 95,
                    MotionState.RUNNING to 88
                )
                
                val (motionState, confidence) = states.random()
                
                logger.debug(LogCategory.LOCATION, "$TAG: Simulating motion state: $motionState")
                val motionEvent = MotionEvent(
                    motionState = motionState,
                    confidence = confidence,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
                _motionEvents.tryEmit(motionEvent)
                
                // Пауза между событиями
                delay(30000) // 30 секунд между событиями
            }
        }
    }

    actual fun stopTracking() {
        if (!isTracking) {
            logger.debug(LogCategory.LOCATION, "$TAG: Not tracking motion")
            return
        }
        
        isTracking = false
        logger.info(LogCategory.LOCATION, "$TAG: Stopped motion tracking")
    }

    actual fun clear() {
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared")
    }

    actual fun destroy() {
        stopTracking()
        logger.debug(LogCategory.LOCATION, "$TAG: Destroyed")
    }
}
