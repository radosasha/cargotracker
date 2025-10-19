package com.shiplocate.data.datasource

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Android реализация получения текущего Firebase токена
 */
actual suspend fun getCurrentTokenFromPlatformImpl(): String? {
    return try {
        println("Android: Getting current Firebase token...")
        val token = FirebaseMessaging.getInstance().token.await()
        println("Android: Got Firebase token: ${token.take(20)}...")
        token
    } catch (e: Exception) {
        println("Android: Failed to get Firebase token: ${e.message}")
        null
    }
}
