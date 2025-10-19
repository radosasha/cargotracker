package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS реализация FirebaseTokenServiceDataSource
 */
actual class FirebaseTokenServiceDataSource {
    
    private val _newTokenFlow = MutableSharedFlow<String>()
    
    // Callback для получения токена от Swift кода
    private var tokenCallback: ((String?) -> Unit)? = null
    
    actual suspend fun getCurrentToken(): String? {
        return suspendCancellableCoroutine { continuation ->
            println("iOS: Requesting current Firebase token from Swift...")
            
            // Сохраняем callback для получения токена
            tokenCallback = { token ->
                println("iOS: Received token from Swift: ${token?.take(20)}...")
                continuation.resume(token)
            }
            
            // TODO: Интегрировать с Swift кодом для получения токена
            // Пока просто логируем - токен будет получен через onNewToken callback
            println("iOS: Token request - will be received via callback")
        }
    }
    
    actual fun getNewTokenFlow(): Flow<String> {
        return _newTokenFlow.asSharedFlow()
    }
    
    actual fun onNewTokenReceived(token: String) {
        println("iOS: New Firebase token received: ${token.take(20)}...")
        _newTokenFlow.tryEmit(token)
    }
    
    actual fun onPushNotificationReceived(userInfo: Map<String, Any>) {
        println("iOS: Push notification received: $userInfo")
        // TODO: Implement push notification handling
    }
    
    /**
     * Вызывается из Swift кода для передачи токена
     */
    fun onTokenReceived(token: String?) {
        tokenCallback?.invoke(token)
        tokenCallback = null
    }
}
