package com.shiplocate.data.service.platform

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android Firebase Messaging Service
 * Только получает события от Firebase и передает в DataSource
 */
class AndroidFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {
    private val firebaseTokenServiceDataSource: FirebaseTokenServiceDataSource by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Android: New Firebase token received: $token")

        // Передаем токен в DataSource (Data Layer)
        firebaseTokenServiceDataSource.onNewTokenReceived(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("Android: Firebase message received: ${remoteMessage.data}")

        // Передаем уведомление в DataSource (Data Layer)
        firebaseTokenServiceDataSource.onPushNotificationReceived(remoteMessage.data)
    }
}
