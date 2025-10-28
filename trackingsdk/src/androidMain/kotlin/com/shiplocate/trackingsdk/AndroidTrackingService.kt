package com.shiplocate.trackingsdk

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.coroutineScope
import com.shiplocate.domain.util.DateFormatter
import com.shiplocate.trackingsdk.di.TrackingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidTrackingService : LifecycleService(), KoinComponent {
    private val binder = LocationBinder()
    private var isTracking = false

    // Koin DI - используем новые Use Cases
    private val trackingManager: TrackingManager by inject()

    companion object Companion {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
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
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationTracking()
        return START_STICKY // Перезапуск сервиса если он был убит системой
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Уведомления о трекинге GPS"
                    setShowBadge(false)
                }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
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
            .setContentTitle("GPS Трекинг активен")
            .setContentText("Приложение отслеживает ваше местоположение")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startLocationTracking() {
        if (isTracking) {
            println("LocationTrackingService: Already tracking, ignoring start request")
            return
        }

        lifecycle.coroutineScope.launch {
            try {
                println("LocationTrackingService: Starting GPS tracking through StartProcessLocationsUseCase")

                // Запускаем обработку GPS координат и подписываемся на Flow результатов
                trackingManager.startTracking()
                    .flowOn(Dispatchers.IO)
                    .onEach { result ->
                        // Обновляем уведомление с актуаьной статистикой
                        updateNotificationWithStats(result.trackingStats)

                        // Логируем результат обработки
                        if (result.shouldSend) {
                            println("AndroidTrackingService: ✅ Location processed successfully: ${result.reason}")
                        } else {
                            println("AndroidTrackingService: ⏭️ Location filtered: ${result.reason}")
                        }
                    }
                    .launchIn(lifecycle.coroutineScope)
                isTracking = true

                println("LocationTrackingService: ✅ GPS tracking started successfully")
                updateNotificationWithStats(com.shiplocate.domain.model.TrackingStats(isTracking = true)) // Initial empty stats
            } catch (e: Exception) {
                println("LocationTrackingService: ❌ Error starting GPS tracking: ${e.message}")
                e.printStackTrace()
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
            println("LocationTrackingService: Not tracking, ignoring stop request")
            return
        }

        try {
            println("LocationTrackingService: Stopping GPS tracking synchronously")

            // Останавливаем обработку GPS координат синхронно
            val result = trackingManager.stopTracking()
            if (result.isSuccess) {
                isTracking = false
                println("LocationTrackingService: ✅ GPS tracking stopped successfully")
            } else {
                println("LocationTrackingService: ❌ Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            println("LocationTrackingService: ❌ Error stopping GPS tracking: ${e.message}")
            e.printStackTrace()
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

        println("LocationTrackingService: Service scope cancelled")
        super.onDestroy()
    }

    fun isLocationTrackingActive(): Boolean = isTracking
}
