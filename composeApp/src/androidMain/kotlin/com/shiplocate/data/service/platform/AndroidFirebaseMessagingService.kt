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
import com.shiplocate.domain.repository.NotificationRepository
import com.shiplocate.domain.usecase.HandlePushNotificationWhenAppKilledUseCase
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
    private val notificationRepository: NotificationRepository by inject()
    private val handlePushNotificationWhenAppKilledUseCase: HandlePushNotificationWhenAppKilledUseCase by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val NOTIFICATION_TYPE_NEW_LOAD: Int = 0
    private val NOTIFICATION_TYPE_LOAD_ASSIGNED: Int = 1
    private val NOTIFICATION_TYPE_LOAD_UPDATED: Int = 2
    private val NOTIFICATION_TYPE_STOP_ENTERED: Int = 3
    private val NOTIFICATION_TYPE_LOAD_UNAVAILABLE: Int = 4
    private val NOTIFICATION_TYPE_SILENT_PUSH: Int = 5

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Android: New Firebase token received: $token")

        scope.launch {
            // Передаем токен в Repository
            notificationRepository.onNewTokenReceived(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("Android: Firebase message received: ${remoteMessage.data}")

        // Уведомляем о получении push (для случая когда приложение запущено)
        scope.launch {
            notificationRepository.pushReceived()
        }

        // Обрабатываем push когда приложение не запущено (onMessageReceived вызывается даже когда app killed, если есть data payload)
        scope.launch {
            try {
                handlePushNotificationWhenAppKilledUseCase()
            } catch (e: Exception) {
                println("Android: Failed to handle push notification when app killed: ${e.message}")
            }
        }

        val shouldShowNotification = try {
            val type = remoteMessage.data["type"]?.toInt()
            type == NOTIFICATION_TYPE_SILENT_PUSH
        } catch (e: Exception) {
            false
        }
        if (shouldShowNotification) { // Показываем уведомление в foreground вручную
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
        } else {
            // TODO
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
