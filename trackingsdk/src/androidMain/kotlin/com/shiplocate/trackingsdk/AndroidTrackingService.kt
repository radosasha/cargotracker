package com.shiplocate.trackingsdk

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.coroutineScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.util.DateFormatter
import com.shiplocate.trackingsdk.motion.models.MotionAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidTrackingService : LifecycleService(), KoinComponent {
    private val binder = LocationBinder()
    private var isTracking = false
    private val logger: Logger by inject()

    // Koin DI - используем новые Use Cases
    private val trackingManager: TrackingManager by inject()

    companion object Companion {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        
        /**
         * Ключ для передачи loadId через Intent
         */
        const val EXTRA_LOAD_ID = "loadId"
    }

    inner class LocationBinder : Binder() {
        fun getService(): AndroidTrackingService = this@AndroidTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        // Получаем loadId из Intent
        val loadId = intent?.getLongExtra(EXTRA_LOAD_ID, -1L) ?: -1L
        if (loadId == -1L) {
            logger.warn(LogCategory.LOCATION, "AndroidTrackingService: No loadId provided in Intent, cannot start tracking")
            stopSelf()
            return START_NOT_STICKY
        }
        
        startLocationTracking(loadId)
        return START_STICKY // Перезапуск сервиса если он был убит системой
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "GPS tracker notifications"
                setShowBadge(false)
            }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        // Используем рефлексию для получения MainActivity
        val mainActivityClass = try {
            Class.forName("com.shiplocate.MainActivity")
        } catch (e: ClassNotFoundException) {
            null
        }

        val intent = if (mainActivityClass != null) {
            Intent(this, mainActivityClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking is active")
            .setContentText("App is tracking your position")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Make the notification immediate for foreground service on Android 12+
                    foregroundServiceBehavior = FOREGROUND_SERVICE_IMMEDIATE
                }
            }
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startLocationTracking(loadId: Long) {
        if (isTracking) {
            logger.debug(LogCategory.LOCATION, "LocationTrackingService: Already tracking, ignoring start request")
            return
        }

        lifecycle.coroutineScope.launch {
            try {
                logger.info(LogCategory.LOCATION, "LocationTrackingService: Starting GPS tracking with loadId=$loadId")

                // Запускаем обработку GPS координат и подписываемся на Flow результатов
                trackingManager.startTracking(loadId)
                    .flowOn(Dispatchers.IO)
                    .onEach { event ->
                        when (event) {
                            is TrackingStateEvent.LocationProcessed -> {
                                // Обновляем уведомление с актуальной статистикой GPS
                                updateNotificationWithStats(event.result.trackingStats)

                                // Логируем результат обработки
                                if (event.result.shouldSend) {
                                    logger.info(LogCategory.LOCATION, "AndroidTrackingService: Location processed: ${event.result.reason}")
                                } else {
                                    logger.debug(LogCategory.LOCATION, "AndroidTrackingService: Location filtered: ${event.result.reason}")
                                }
                            }

                            is TrackingStateEvent.MotionAnalysis -> {
                                // Обновляем уведомление с результатами анализа движения
                                updateNotificationWithMotionAnalysis(event.analysisResult, event.timestamp)

                                logger.debug(
                                    LogCategory.LOCATION,
                                    "AndroidTrackingService: Motion analysis: driving=${event.analysisResult.drivingDetected}, " +
                                        "vehicleTime=${(event.analysisResult.vehicleTimePercentage * 100).toInt()}%, " +
                                        "confidence=${event.analysisResult.averageConfidence}%",
                                )
                            }
                        }
                    }
                    .launchIn(lifecycle.coroutineScope)
                isTracking = true

                logger.info(LogCategory.LOCATION, "LocationTrackingService: GPS tracking started successfully")
                updateNotificationWithStats(com.shiplocate.domain.model.TrackingStats(isTracking = true)) // Initial empty stats
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "LocationTrackingService: Error starting GPS tracking", e)
                updateNotificationWithStats(com.shiplocate.domain.model.TrackingStats(isTracking = false)) // Error state
                stopSelf()
            }
        }
    }

    /**
     * Синхронная остановка GPS трекинга для использования в onDestroy()
     * Избегает race condition при отмене serviceScope
     */
    private suspend fun stopLocationTrackingSync() {
        if (!isTracking) {
            logger.debug(LogCategory.LOCATION, "LocationTrackingService: Not tracking, ignoring stop request")
            return
        }

        try {
            logger.info(LogCategory.LOCATION, "LocationTrackingService: Stopping GPS tracking")
            val result = trackingManager.stopTracking()
            if (result.isSuccess) {
                isTracking = false
                logger.info(LogCategory.LOCATION, "LocationTrackingService: GPS tracking stopped successfully")
            } else {
                logger.error(
                    LogCategory.LOCATION,
                    "LocationTrackingService: Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}",
                    result.exceptionOrNull(),
                )
            }
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "LocationTrackingService: Error stopping GPS tracking", e)
        }
    }

    /**
     * Обновляет уведомление с результатами анализа движения
     */
    private fun updateNotificationWithMotionAnalysis(
        analysis: MotionAnalysisResult,
        timestamp: Long,
    ) {
        val motionText = buildString {
            append("Motion Analysis Results:\n\n")
            append("Driving Detected: ${if (analysis.drivingDetected) "✅ YES" else "❌ NO"}\n")
            append("Vehicle Time: ${(analysis.vehicleTimePercentage * 100).toInt()}%\n")
            append("Avg Confidence: ${analysis.averageConfidence}%\n")
            append("Events Analyzed: ${analysis.eventsAnalyzed}\n")
            append("Consecutive Driving: ${analysis.consecutiveDrivingCount}\n")
            append("Consecutive Non-Driving: ${analysis.consecutiveNonDrivingCount}\n")

            analysis.statistics?.let { stats ->
                append("\nDetailed Stats:\n")
                append("Total Time: ${stats.totalTimeMs / 1000}s\n")
                append("Vehicle Time: ${stats.vehicleTimeMs / 1000}s\n")
                append("Walking Time: ${stats.walkingTimeMs / 1000}s\n")
                append("Stationary Time: ${stats.stationaryTimeMs / 1000}s\n")
                append("Last Activity: ${stats.lastActivity}\n")
            }

            append("\nTime: ${DateFormatter.formatForNotification(Instant.fromEpochMilliseconds(timestamp))}")
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Motion Analysis")
            .setContentText(
                "Driving: ${if (analysis.drivingDetected) "YES" else "NO"} | " +
                    "Vehicle: ${(analysis.vehicleTimePercentage * 100).toInt()}%"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(motionText),
            )
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Обновляет уведомление с подробной статистикой
     */
    private fun updateNotificationWithStats(stats: com.shiplocate.domain.model.TrackingStats) {
        val locationText =
            buildString {
                append("Saved: ${stats.totalSaved} | Sent: ${stats.totalSent} | Filtered: ${stats.totalFiltered}\n\n")

                stats.lastFilteredLocation?.let { location ->
                    append("🚫 Last Filtered: ")
                    location.accuracy?.let {
                        append("Accuracy: ${String.format("%.1f", it)}m\n")
                    }
                    append("Reason: ${location.filterReason}\n")
                    append("Time: ${DateFormatter.formatForNotification(location.timestamp)}\n\n")
                }

                stats.lastSentLocation?.let { location ->
                    append("📤 Last Sent: ")
                    location.accuracy?.let {
                        append("Accuracy: ${String.format("%.1f", it)}m\n")
                    }
                    append("Time: ${DateFormatter.formatForNotification(location.timestamp)}")
                } ?: stats.lastSendError?.let { error ->
                    append("❌ Last Send Error: ")
                    error.accuracy?.let {
                        append("Accuracy: ${String.format("%.1f", it)}m\n")
                    }
                    append("Error: ${error.errorMessage}\n")
                    append("Time: ${DateFormatter.formatForNotification(error.timestamp)}")
                } ?: run {
                    append("📤 Last Sent: None yet")
                }
            }

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GPS Трекинг активен")
                .setContentText("Saved: ${stats.totalSaved} | Sent: ${stats.totalSent} | Filtered: ${stats.totalFiltered}")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(locationText),
                )
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()

        val notificationManager = NotificationManagerCompat.from(this)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        // Синхронная остановка GPS трекинга перед отменой scope
        if (isTracking) {
            // Используем runBlocking для синхронного выполнения
            runBlocking {
                stopLocationTrackingSync()
            }
        }

        logger.warn(LogCategory.LOCATION, "LocationTrackingService: Service scope cancelled")
        super.onDestroy()
    }
}
