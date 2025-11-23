package com.shiplocate.data.datasource

import com.google.firebase.messaging.FirebaseMessaging
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.tasks.await

/**
 * Android реализация FirebaseTokenServiceDataSource
 */
actual class FirebaseTokenServiceDataSource(
    private val logger: Logger
) {

    private val _newTokenFlow = MutableSharedFlow<String>()

    actual suspend fun getCurrentToken(): String? {
        return try {
            logger.debug(LogCategory.GENERAL, "Android: Getting current Firebase token...")
            val token = FirebaseMessaging.getInstance().token.await()
            logger.debug(LogCategory.GENERAL, "Android: Got Firebase token on-demand: ${token.take(20)}...")
            token
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "Android: Failed to get Firebase token: ${e.message}", e)
            null
        }
    }
    
    actual fun getNewTokenFlow(): Flow<String> {
        return _newTokenFlow
    }

    actual suspend fun onNewTokenReceived(token: String) {
        logger.info(LogCategory.GENERAL, "Android: New Firebase token received: ${token.take(20)}...")
        _newTokenFlow.emit(token)
    }
}
