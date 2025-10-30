package com.shiplocate.trackingsdk.motion

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Android actual реализация ActivityRecognitionConnector
 */
actual class ActivityRecognitionConnector(
    private val activityFrequencyMs: Long,
    private val context: Context,
    private val activityRecognitionClient: ActivityRecognitionClient,
    private val logger: Logger,
    private val scope: CoroutineScope,
) {

    private var isTracking = false

    // Flow для отправки событий движения
    private val motionEvents = MutableSharedFlow<MotionEvent>(replay = 0)

    private val activityRecognitionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTIVITY_TRANSITION_ACTION) {
                val activities = ActivityRecognitionResult.extractResult(intent)?.probableActivities
                activities?.forEach { activity ->
                    scope.launch {
                        // Конвертируем DetectedActivity в MotionEvent с реальными вероятностями
                        val motionState = when (activity.type) {
                            DetectedActivity.IN_VEHICLE -> MotionState.IN_VEHICLE
                            DetectedActivity.ON_BICYCLE -> MotionState.ON_BICYCLE
                            DetectedActivity.WALKING -> MotionState.WALKING
                            DetectedActivity.RUNNING -> MotionState.RUNNING
                            DetectedActivity.STILL -> MotionState.STATIONARY
                            else -> MotionState.UNKNOWN
                        }

                        // Используем реальную вероятность от ActivityRecognition
                        val confidence = activity.confidence

                        logger.debug(
                            LogCategory.LOCATION,
                            "$TAG: Activity recognition: ${activity.type} -> $motionState (confidence: $confidence%)"
                        )

                        val motionEvent = MotionEvent(
                            motionState = motionState,
                            confidence = confidence,
                            timestamp = System.currentTimeMillis()
                        )
                        if (isTracking) {
                            motionEvents.tryEmit(motionEvent)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = ActivityRecognitionConnector::class.simpleName
        private const val REQUEST_CODE = 1001
        private const val ACTIVITY_TRANSITION_ACTION = "com.shiplocate.trackingsdk.ACTIVITY_TRANSITION"
    }

    init {
        // Регистрируем BroadcastReceiver для получения событий ActivityRecognition
        val intentFilter = IntentFilter(ACTIVITY_TRANSITION_ACTION)
        context.registerReceiver(activityRecognitionReceiver, intentFilter)
    }

    actual fun startTracking() {
        if (isTracking) {
            logger.debug(LogCategory.LOCATION, "$TAG: Already tracking motion")
            return
        }

        isTracking = true
        logger.info(LogCategory.LOCATION, "$TAG: Starting ActivityRecognition")

        try {
            // Создаем Intent для BroadcastReceiver
            val intent = Intent(ACTIVITY_TRANSITION_ACTION)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Запускаем ActivityRecognition с интервалом обновления 10 секунд
            val task = activityRecognitionClient.requestActivityUpdates(
                activityFrequencyMs, // 10 секунд
                pendingIntent
            )

            task.addOnSuccessListener {
                logger.info(LogCategory.LOCATION, "$TAG: ActivityRecognition started successfully")
            }.addOnFailureListener { exception ->
                logger.error(LogCategory.LOCATION, "$TAG: Failed to start ActivityRecognition: ${exception.message}", exception)
                isTracking = false
            }

        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: Error starting ActivityRecognition: ${e.message}", e)
            isTracking = false
        }
    }

    actual fun stopTracking() {
        if (!isTracking) {
            logger.debug(LogCategory.LOCATION, "$TAG: Not tracking motion")
            return
        }

        try {
            val intent = Intent(ACTIVITY_TRANSITION_ACTION)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            activityRecognitionClient.removeActivityUpdates(pendingIntent)
                .addOnSuccessListener {
                    isTracking = false
                    logger.info(LogCategory.LOCATION, "$TAG: Successfully stopped ActivityRecognition")
                }
                .addOnFailureListener { exception ->
                    logger.error(LogCategory.LOCATION, "$TAG: Failed to stop ActivityRecognition: ${exception.message}", exception)
                }

        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: Error stopping ActivityRecognition: ${e.message}", e)
        }
    }

    actual fun clear() {
        // Очистка не нужна для ActivityRecognition
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared")
    }

    actual fun destroy() {
        stopTracking()
        try {
            context.unregisterReceiver(activityRecognitionReceiver)
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: Error unregistering receiver: ${e.message}", e)
        }
        logger.debug(LogCategory.LOCATION, "$TAG: Destroyed")
    }

    actual fun observeMotionEvents(): Flow<MotionEvent> = motionEvents
}
