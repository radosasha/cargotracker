package com.tracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tracker.domain.usecase.StartProcessLocationsUseCase
import com.tracker.domain.usecase.StopProcessLocationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class AndroidTrackingService : Service(), KoinComponent {
    
    private val binder = LocationBinder()
    private var isTracking = false
    
    // Koin DI - используем новые Use Cases
    private val startProcessLocationsUseCase: StartProcessLocationsUseCase by inject()
    private val stopProcessLocationsUseCase: StopProcessLocationsUseCase by inject()
    
    // Coroutine scope for network operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
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
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationTracking()
        return START_STICKY // Перезапуск сервиса если он был убит системой
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления о трекинге GPS"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
        
        try {
            println("LocationTrackingService: Starting GPS tracking through StartProcessLocationsUseCase")
            
            // Запускаем обработку GPS координат через StartProcessLocationsUseCase с serviceScope
            startProcessLocationsUseCase(serviceScope)
            isTracking = true
            
            println("LocationTrackingService: ✅ GPS tracking started successfully")
            updateNotification("GPS tracking started")
            
        } catch (e: Exception) {
            println("LocationTrackingService: ❌ Error starting GPS tracking: ${e.message}")
            e.printStackTrace()
            updateNotification("Error starting GPS tracking")
            stopSelf()
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
            val result = stopProcessLocationsUseCase()
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
    
    
    private fun updateNotification(status: String = "GPS tracking active") {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Трекинг активен")
            .setContentText(status)
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
        super.onDestroy()
        
        // Синхронная остановка GPS трекинга перед отменой scope
        if (isTracking) {
            try {
                // Используем runBlocking для синхронного выполнения
                runBlocking {
                    stopLocationTrackingSync()
                }
            } catch (e: Exception) {
                println("LocationTrackingService: Error in runBlocking: ${e.message}")
            }
        }
        
        // Останавливаем serviceScope, что отменит все корутины
        serviceScope.cancel()
        println("LocationTrackingService: Service scope cancelled")
    }
    
    fun isLocationTrackingActive(): Boolean = isTracking
}