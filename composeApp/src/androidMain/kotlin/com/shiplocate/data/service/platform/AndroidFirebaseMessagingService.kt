package com.shiplocate.data.service.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

/**
 * Android Firebase Messaging Service
 * Только получает события от Firebase и передает в DataSource
 */
class AndroidFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {
    private val firebaseTokenServiceDataSource: FirebaseTokenServiceDataSource by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Android: New Firebase token received: $token")

        scope.launch {
            // Передаем токен в DataSource (Data Layer)
            firebaseTokenServiceDataSource.onNewTokenReceived(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("Android: Firebase message received: ${remoteMessage.data}")

        // Передаем уведомление в DataSource (Data Layer)
        firebaseTokenServiceDataSource.onPushNotificationReceived(remoteMessage.data)

        // Показываем уведомление в foreground вручную
        try {
            val title = remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: "Notification"
            val body = remoteMessage.notification?.body
                ?: remoteMessage.data["body"]
                ?: remoteMessage.data["command"]
                ?: ""

            showForegroundNotification(title, body)
        } catch (e: Exception) {
            println("Android: failed to show foreground notification: ${e.message}")
        }
    }

    private fun showForegroundNotification(title: String, body: String) {
        val channelId = "default_notifications"
        val channelName = "General"

        val manager = getSystemService(NotificationManager::class.java)
        if (manager?.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH,
            )
            manager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // игнорируем если нет пермишенов на показ увеомлений
            return
        }
        NotificationManagerCompat.from(this).notify(Random.nextInt(), builder.build())
    }
}
