package com.shiplocate.trackingsdk.motion

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.ActivityRecognitionClient
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

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

    // События доставляются explicit BroadcastReceiver'ом (ActivityUpdatesReceiver) через PendingIntent

    companion object {
        private val TAG = ActivityRecognitionConnector::class.simpleName
        private const val REQUEST_CODE = 1001
        private const val ACTIVITY_TRANSITION_ACTION = "com.shiplocate.trackingsdk.ACTIVITY_TRANSITION"
    }

    // Dynamic registerReceiver не используется — применяем explicit receiver через PendingIntent

    actual fun startTracking() {
        if (isTracking) {
            logger.debug(LogCategory.LOCATION, "$TAG: Already tracking motion")
            return
        }

        isTracking = true
        logger.info(LogCategory.LOCATION, "$TAG: Starting ActivityRecognition")

        try {
            // Запускаем ActivityRecognition с интервалом обновления activityFrequencyMs
            val task = activityRecognitionClient.requestActivityUpdates(
                activityFrequencyMs, // 10 секунд
                createPendingIntent(context)
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
            activityRecognitionClient.removeActivityUpdates(createPendingIntent(context))
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

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ActivityUpdatesReceiver::class.java).apply {
            action = ACTIVITY_TRANSITION_ACTION
        }

        val piFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            piFlags
        )
        return pendingIntent
    }

    actual fun clear() {
        // Очистка не нужна для ActivityRecognition
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared")
    }

    actual fun destroy() {
        stopTracking()
        // Нет динамически зарегистрированного ресивера — ничего не отписываем
        logger.debug(LogCategory.LOCATION, "$TAG: Destroyed")
    }

    actual fun observeMotionEvents(): Flow<MotionEvent> = MotionEventBus.flow
}
