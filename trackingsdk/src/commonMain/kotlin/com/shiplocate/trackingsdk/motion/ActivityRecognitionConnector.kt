package com.shiplocate.trackingsdk.motion

import com.shiplocate.trackingsdk.motion.models.MotionEvent
import kotlinx.coroutines.flow.SharedFlow

/**
 * Expect/actual интерфейс для ActivityRecognitionConnector
 * Предоставляет платформо-специфичную реализацию отслеживания движения
 */
expect class ActivityRecognitionConnector {
    val motionEvents: SharedFlow<MotionEvent>
    fun startTracking()
    fun stopTracking()
    fun clear()
    fun destroy()
}
