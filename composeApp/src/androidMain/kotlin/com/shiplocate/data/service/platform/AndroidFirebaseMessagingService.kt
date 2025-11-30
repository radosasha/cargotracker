package com.shiplocate.data.service.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shiplocate.MainActivity
import com.shiplocate.domain.model.notification.NotificationPayloadKeys
import com.shiplocate.domain.model.notification.NotificationType
import com.shiplocate.domain.repository.NotificationRepository
import com.shiplocate.domain.usecase.HandlePushNotificationWhenAppKilledUseCase
import com.shiplocate.domain.usecase.logs.GetLogsClientIdUseCase
import com.shiplocate.domain.usecase.logs.SendAllLogsUseCase
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
    private val sendAllLogsUseCase: SendAllLogsUseCase by inject()
    private val getLogsClientIdUseCase: GetLogsClientIdUseCase by inject()


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    companion object {
        private const val DEFAULT_NOTIFICATION_CHANNEL_ID = "default_notifications"
        private const val DEFAULT_NOTIFICATION_CHANNEL_NAME = "General"
    }

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

        val notificationType =
            remoteMessage.data[NotificationPayloadKeys.TYPE]?.toIntOrNull()

        // Уведомляем о получении push (для случая когда приложение запущено)
        scope.launch {
            notificationRepository.pushReceived(notificationType)
        }

        // Обрабатываем push когда приложение не запущено (onMessageReceived вызывается даже когда app killed, если есть data payload)
        scope.launch {
            try {
                handlePushNotificationWhenAppKilledUseCase()
            } catch (e: Exception) {
                println("Android: Failed to handle push notification when app killed: ${e.message}")
            }
        }

        val shouldShowNotification = notificationType != NotificationType.SILENT
        if (shouldShowNotification) { // Показываем уведомление в foreground вручную
            try {
                val title = remoteMessage.notification?.title
                    ?: remoteMessage.data["title"]
                    ?: "Notification"
                val body = remoteMessage.notification?.body
                    ?: remoteMessage.data["body"]
                    ?: remoteMessage.data["command"]
                    ?: ""

                showForegroundNotification(title, body, remoteMessage.data)
            } catch (e: Exception) {
                println("Android: failed to show foreground notification: ${e.message}")
            }
        } else {
            scope.launch {
                val clientId = getLogsClientIdUseCase()
                sendAllLogsUseCase(clientId)
            }
        }
    }

    private fun showForegroundNotification(
        title: String,
        body: String,
        payload: Map<String, String>,
    ) {
        val channelId = DEFAULT_NOTIFICATION_CHANNEL_ID
        val channelName = DEFAULT_NOTIFICATION_CHANNEL_NAME

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

        val launchIntent =
            Intent(this, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                payload[NotificationPayloadKeys.TYPE]?.let { putExtra(NotificationPayloadKeys.TYPE, it) }
                payload[NotificationPayloadKeys.LOAD_ID]?.let { putExtra(NotificationPayloadKeys.LOAD_ID, it) }
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                Random.nextInt(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        builder.setContentIntent(pendingIntent)

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
