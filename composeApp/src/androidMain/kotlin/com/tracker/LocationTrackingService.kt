package com.tracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location as AndroidLocation
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tracker.domain.model.Location
import com.tracker.domain.usecase.ProcessLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class LocationTrackingService : Service(), LocationListener, KoinComponent {
    
    private val binder = LocationBinder()
    private lateinit var locationManager: LocationManager
    private var isTracking = false
    
    // Koin DI
    private val processLocationUseCase: ProcessLocationUseCase by inject()
    
    // Coroutine scope for network operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        
        // Интервалы обновления GPS (в миллисекундах)
        private const val MIN_TIME_MS = 60 * 1000L // 1 минута
        private const val MIN_DISTANCE_M = 500f // 500 метров
    }
    
    inner class LocationBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }
    
    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        if (isTracking) return
        
        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        
        try {
            // Запрашиваем обновления GPS
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                this,
                Looper.getMainLooper()
            )
            
            // Также используем Network Provider как резерв
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                this,
                Looper.getMainLooper()
            )
            
            isTracking = true
        } catch (e: SecurityException) {
            e.printStackTrace()
            stopSelf()
        }
    }
    
    private fun stopLocationTracking() {
        if (!isTracking) return
        
        locationManager.removeUpdates(this)
        isTracking = false
    }
    
    override fun onLocationChanged(androidLocation: AndroidLocation) {
        println("LocationTrackingService: GPS Location received")
        println("LocationTrackingService: Lat: ${androidLocation.latitude}, Lon: ${androidLocation.longitude}")
        println("LocationTrackingService: Accuracy: ${androidLocation.accuracy}m, Time: ${androidLocation.time}")
        println("LocationTrackingService: Speed: ${if (androidLocation.hasSpeed()) androidLocation.speed else "N/A"} m/s")
        println("LocationTrackingService: Bearing: ${if (androidLocation.hasBearing()) androidLocation.bearing else "N/A"}°")
        
        // Конвертируем Android Location в Domain Location
        val domainLocation = Location(
            latitude = androidLocation.latitude,
            longitude = androidLocation.longitude,
            accuracy = androidLocation.accuracy,
            altitude = if (androidLocation.hasAltitude()) androidLocation.altitude else null,
            speed = if (androidLocation.hasSpeed()) androidLocation.speed else null,
            bearing = if (androidLocation.hasBearing()) androidLocation.bearing else null,
            timestamp = kotlinx.datetime.Instant.fromEpochMilliseconds(androidLocation.time),
            deviceId = "40329715"
        )
        
        // Получаем уровень батареи
        val batteryLevel = getBatteryLevel()
        
        // Обрабатываем координату через Use Case
        serviceScope.launch {
            try {
                val result = processLocationUseCase(domainLocation, batteryLevel)
                
                if (result.shouldSend) {
                    println("LocationTrackingService: ✅ Successfully processed location")
                    println("LocationTrackingService: Reason: ${result.reason}")
                    println("LocationTrackingService: Battery: ${batteryLevel?.let { "${it}%" } ?: "N/A"}")
                    updateNotification(androidLocation, "Location saved to DB")
                } else {
                    println("LocationTrackingService: ⏭️ Location filtered out")
                    println("LocationTrackingService: Reason: ${result.reason}")
                    updateNotification(androidLocation, "Location filtered")
                }
                
                println("LocationTrackingService: Total sent: ${result.totalSent}, Total received: ${result.totalReceived}")
                
            } catch (e: Exception) {
                println("LocationTrackingService: ❌ Error processing location: ${e.message}")
                e.printStackTrace()
                updateNotification(androidLocation, "Error processing location")
            }
        }
    }
    
    private fun getBatteryLevel(): Float? {
        return try {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
            batteryManager?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)?.toFloat()
        } catch (e: Exception) {
            println("LocationTrackingService: Error getting battery level: ${e.message}")
            null
        }
    }
    
    private fun updateNotification(androidLocation: AndroidLocation, status: String = "Location received") {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Трекинг активен")
            .setContentText("$status: ${androidLocation.latitude}, ${androidLocation.longitude}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$status\n" +
                        "Координаты: ${androidLocation.latitude}, ${androidLocation.longitude}\n" +
                        "Точность: ${androidLocation.accuracy}m\n" +
                        "Время: ${androidLocation.time}"))
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
        stopLocationTracking()
    }
    
    fun isLocationTrackingActive(): Boolean = isTracking
}
