package com.shiplocate.data.datasource

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await

/**
 * Android реализация FirebaseTokenServiceDataSource
 */
actual class FirebaseTokenServiceDataSource {

    private val _newTokenFlow = MutableSharedFlow<String>()

    actual suspend fun getCurrentToken(): String? {
        return try {
            println("Android: Getting current Firebase token...")
            val token = FirebaseMessaging.getInstance().token.await()
            println("Android: Got Firebase token on-demand: ${token.take(20)}...")
            token
        } catch (e: Exception) {
            println("Android: Failed to get Firebase token: ${e.message}")
            null
        }
    }
    
    actual fun getNewTokenFlow(): Flow<String> {
        return _newTokenFlow
    }

    actual suspend fun onNewTokenReceived(token: String) {
        println("Android: New Firebase token received: ${token.take(20)}...")
        _newTokenFlow.emit(token)
    }
    
    actual fun onPushNotificationReceived(userInfo: Map<String, Any>) {
        println("Android: Push notification received: $userInfo")
        // TODO: Implement push notification handling
    }
}
