package com.shiplocate.trackingsdk

import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.trackingsdk.motion.models.MotionAnalysisResult

/**
 * Объединенное событие состояния трекинга
 * Может содержать либо результаты обработки GPS координат, либо результаты анализа движения
 */
sealed class TrackingStateEvent {
    /**
     * Результат обработки GPS координаты от TripRecorder
     */
    data class LocationProcessed(
        val result: LocationProcessResult,
    ) : TrackingStateEvent()

    /**
     * Результаты анализа движения от MotionTracker
     */
    data class MotionAnalysis(
        val analysisResult: MotionAnalysisResult,
        val timestamp: Long,
    ) : TrackingStateEvent()
}

