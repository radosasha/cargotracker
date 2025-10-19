package com.shiplocate.data.service.platform

import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS Firebase Messaging Delegate
 * Только получает события от Firebase и передает в DataSource
 */
class IOSFirebaseMessagingDelegate : KoinComponent {
    private val firebaseTokenServiceDataSource: FirebaseTokenServiceDataSource by inject()

    /**
     * Вызывается из iOSApp.swift когда Firebase получает новый токен
     */
    fun onNewTokenReceived(token: String) {
        println("iOS: New Firebase token received: $token")

        // Передаем токен в DataSource (Data Layer)
        firebaseTokenServiceDataSource.onNewTokenReceived(token)
    }

    fun onPushNotificationReceived(userInfo: Map<String, Any>) {
        println("iOS: Push notification received: $userInfo")
        firebaseTokenServiceDataSource.onPushNotificationReceived(userInfo)
    }
}
