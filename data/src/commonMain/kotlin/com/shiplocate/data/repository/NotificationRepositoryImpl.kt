package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.FirebaseTokenRemoteDataSource
import com.shiplocate.data.datasource.FirebaseTokenService
import com.shiplocate.domain.datasource.FirebaseTokenLocalDataSource
import com.shiplocate.domain.repository.NotificationRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Реализация NotificationRepository
 * Координирует работу с DataSources
 */
class NotificationRepositoryImpl(
    private val firebaseTokenLocalDataSource: FirebaseTokenLocalDataSource,
    private val firebaseTokenService: FirebaseTokenService,
    private val firebaseTokenRemoteDataSource: FirebaseTokenRemoteDataSource,
    private val logger: Logger,
) : NotificationRepository {
    private val coroutineScope = MainScope()

    override suspend fun sendTokenToServer(token: String) {
        logger.info(LogCategory.NETWORK, "Repository: Sending token to server")
        
        // Проверяем, есть ли уже такой же токен в кеше и был ли он отправлен
        val cachedToken = firebaseTokenLocalDataSource.getCachedToken()
        val isTokenSent = firebaseTokenLocalDataSource.isTokenSent()
        
        if (cachedToken == token && isTokenSent) {
            logger.info(LogCategory.NETWORK, "Repository: Token already sent to server, skipping")
            return
        }

        try {
            firebaseTokenRemoteDataSource.sendTokenToServer(token)
        } catch (e: Throwable) {
            logger.error(LogCategory.UI, "NotificationRepositoryImpl: Error sending firebase token: ${e.message}")
        }
        
        // Помечаем токен как отправленный
        firebaseTokenLocalDataSource.saveToken(token)
        firebaseTokenLocalDataSource.markTokenAsSent()
    }

    override suspend fun getCachedToken(): String? {
        return firebaseTokenLocalDataSource.getCachedToken()
    }

    override suspend fun sendCachedTokenOnAuth() {
        logger.info(LogCategory.NETWORK, "Repository: Sending cached token on auth")

        val cachedToken = firebaseTokenLocalDataSource.getCachedToken()
        val isSent = firebaseTokenLocalDataSource.isTokenSent()
        
        if (cachedToken != null && !isSent) {
            logger.info(LogCategory.NETWORK, "Repository: Found unsent cached firebase token, sending to server")
            sendTokenToServer(cachedToken)
        } else if (cachedToken != null && isSent) {
            logger.info(LogCategory.NETWORK, "Repository: Cached firebase token already sent to server")
        } else {
            logger.info(LogCategory.NETWORK, "Repository: No cached firebase token found for auth")
        }
    }

    override suspend fun clearToken() {
        try {
            firebaseTokenRemoteDataSource.clearToken()
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "NotificationRepositoryImpl: Failed to clear firebase token${e.message}", e)
        }
    }

    override suspend fun startTokenUpdates() {
        logger.info(LogCategory.NETWORK, "Repository: Starting Firebase token updates")

        // Слушаем новые токены из ServiceDataSource и обрабатываем их
        val flow = firebaseTokenService.getNewTokenFlow()
        logger.debug(LogCategory.NETWORK, "Repository: Flow created: $flow")
        
        flow.onEach { token ->
            logger.debug(LogCategory.NETWORK, "Repository: Flow received token: ${token.take(20)}...")
            if (token.isNotEmpty()) {
                logger.info(LogCategory.NETWORK, "Repository: New Firebase token received: ${token.take(20)}...")

                // Сохраняем токен в локальном кеше
                logger.debug(LogCategory.NETWORK, "Repository: Save Firebase token locally: ${token.take(20)}...")
                val cachedToken = firebaseTokenLocalDataSource.getCachedToken()
                if (cachedToken != token) {
                    firebaseTokenLocalDataSource.saveToken(token)
                }

                // Логика отправки на сервер будет в Use Case
                logger.debug(LogCategory.NETWORK, "Repository: Token saved to local cache")
            }
        }
        .launchIn(coroutineScope)
        
        logger.info(LogCategory.NETWORK, "Repository: Flow subscription launched")
    }

    override fun observeTokenUpdates(): Flow<String> {
        return firebaseTokenLocalDataSource.observeTokenUpdates()
    }

    override suspend fun getCurrentTokenFromFirebase(): String? {
        return firebaseTokenService.getCurrentToken()
    }

    override suspend fun saveToken(token: String) {
        firebaseTokenLocalDataSource.saveToken(token)
    }

    override fun observeReceivedPushes(): Flow<Int?> {
        return firebaseTokenRemoteDataSource.observeReceivedPushes()
    }

    override suspend fun onNewTokenReceived(token: String) {
        firebaseTokenService.onNewTokenReceived(token)
    }

    override suspend fun pushReceived(type: Int?) {
        firebaseTokenRemoteDataSource.pushReceived(type)
    }
}
