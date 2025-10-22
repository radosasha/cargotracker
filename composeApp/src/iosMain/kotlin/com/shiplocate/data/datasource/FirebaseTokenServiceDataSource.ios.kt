package com.shiplocate.data.datasource

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS реализация FirebaseTokenServiceDataSource
 */
actual class FirebaseTokenServiceDataSource(
    private val logger: Logger
) {
    
    private val _newTokenFlow = MutableSharedFlow<String>()
    
    // Callback для получения токена от Swift кода
    private var tokenCallback: ((String?) -> Unit)? = null
    
    actual suspend fun getCurrentToken(): String? {
        return suspendCancellableCoroutine { continuation ->
            logger.debug(LogCategory.GENERAL, "iOS: Requesting current Firebase token from Swift...")
            
            // Сохраняем callback для получения токена
            tokenCallback = { token ->
                logger.debug(LogCategory.GENERAL, "iOS: Received token from Swift: ${token?.take(20)}...")
                continuation.resume(token)
            }
            
            // TODO: Интегрировать с Swift кодом для получения токена
            // Пока просто логируем - токен будет получен через onNewToken callback
            logger.debug(LogCategory.GENERAL, "iOS: Token request - will be received via callback")
        }
    }
    
    actual fun getNewTokenFlow(): Flow<String> {
        return _newTokenFlow
    }

    actual suspend fun onNewTokenReceived(token: String) {
        logger.info(LogCategory.GENERAL, "iOS: New Firebase token received: ${token.take(20)}...")
        _newTokenFlow.emit(token)
    }
    
    actual fun onPushNotificationReceived(userInfo: Map<String, Any>) {
        logger.info(LogCategory.GENERAL, "iOS: Push notification received: $userInfo")
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
