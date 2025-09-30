package com.tracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class LocationTrackingService : Service(), LocationListener {
    
    private val binder = LocationBinder()
    private lateinit var locationManager: LocationManager
    private var isTracking = false
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        
        // Интервалы обновления GPS (в миллисекундах)
        private const val MIN_TIME_MS = 1000L // 1 секунда
        private const val MIN_DISTANCE_M = 1f // 1 метр
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
    
    override fun onLocationChanged(location: Location) {
        // Здесь вы можете обработать полученные GPS координаты
        // Например, отправить на сервер или сохранить локально
        println("GPS Location: ${location.latitude}, ${location.longitude}")
        println("Accuracy: ${location.accuracy}m, Time: ${location.time}")
        
        // Обновляем уведомление с последними координатами
        updateNotification(location)
    }
    
    private fun updateNotification(location: Location) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Трекинг активен")
            .setContentText("Последнее обновление: ${location.latitude}, ${location.longitude}")
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
