package com.shiplocate.data.repository

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
) : NotificationRepository {
    private val coroutineScope = MainScope()

    override suspend fun sendTokenToServer(token: String) {
        println("Repository: Sending token to server")
        
        // Проверяем, есть ли уже такой же токен в кеше и был ли он отправлен
        val cachedToken = firebaseTokenLocalDataSource.getCachedToken()
        val isTokenSent = firebaseTokenLocalDataSource.isTokenSent()
        
        if (cachedToken == token && isTokenSent) {
            println("Repository: Token already sent to server, skipping")
            return
        }

        firebaseTokenRemoteDataSource.sendTokenToServer(token)
        
        // Помечаем токен как отправленный
        firebaseTokenLocalDataSource.saveToken(token)
        firebaseTokenLocalDataSource.markTokenAsSent()
    }

    override suspend fun getCachedToken(): String? {
        return firebaseTokenLocalDataSource.getCachedToken()
    }

    override suspend fun sendCachedTokenOnAuth() {
        println("Repository: Sending cached token on auth")

        val cachedToken = firebaseTokenLocalDataSource.getCachedToken()
        val isSent = firebaseTokenLocalDataSource.isTokenSent()
        
        if (cachedToken != null && !isSent) {
            println("Repository: Found unsent cached firebase token, sending to server")
            sendTokenToServer(cachedToken)
        } else if (cachedToken != null && isSent) {
            println("Repository: Cached firebase token already sent to server")
        } else {
            println("Repository: No cached firebase token found for auth")
        }
    }

    override suspend fun clearToken() {
        firebaseTokenRemoteDataSource.clearToken()
    }

    override suspend fun startTokenUpdates() {
        println("Repository: Starting Firebase token updates")

        // Слушаем новые токены из ServiceDataSource и обрабатываем их
        val flow = firebaseTokenService.getNewTokenFlow()
        println("Repository: Flow created: $flow")
        
        flow.onEach { token ->
            println("Repository: Flow received token: ${token.take(20)}...")
            if (token.isNotEmpty()) {
                println("Repository: New Firebase token received: ${token.take(20)}...")

                // Сохраняем токен в локальном кеше
                println("Repository: Save Firebase token locally: ${token.take(20)}...")
                val cachedToken = firebaseTokenLocalDataSource.getCachedToken()
                if (cachedToken != token) {
                    firebaseTokenLocalDataSource.saveToken(token)
                }

                // Логика отправки на сервер будет в Use Case
                println("Repository: Token saved to local cache")
            }
        }
        .launchIn(coroutineScope)
        
        println("Repository: Flow subscription launched")
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
}
