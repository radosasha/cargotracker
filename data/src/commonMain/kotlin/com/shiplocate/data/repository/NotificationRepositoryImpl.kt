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
        firebaseTokenRemoteDataSource.sendTokenToServer(token)
    }

    override suspend fun getCachedToken(): String? {
        return firebaseTokenLocalDataSource.getCachedToken()
    }

    override suspend fun sendCachedTokenOnAuth() {
        println("Repository: Sending cached token on auth")

        val cachedToken = firebaseTokenLocalDataSource.getCachedToken()
        if (cachedToken != null) {
            sendTokenToServer(cachedToken)
        } else {
            println("No cached token found for auth")
        }
    }

    override suspend fun getTokenStatus(): Boolean {
        return firebaseTokenRemoteDataSource.getTokenStatus()
    }

    override suspend fun clearToken() {
        firebaseTokenRemoteDataSource.clearToken()
    }

    override suspend fun startTokenUpdates() {
        println("Repository: Starting Firebase token updates")

        // Слушаем новые токены из ServiceDataSource и обрабатываем их
        firebaseTokenService.getNewTokenFlow()
            .onEach { token ->
                if (token.isNotEmpty()) {
                    println("Repository: New Firebase token received: ${token.take(20)}...")

                    // Сохраняем токен в локальном кеше
                    firebaseTokenLocalDataSource.saveToken(token)

                    // Логика отправки на сервер будет в Use Case
                    println("Repository: Token saved to local cache")
                }
            }
            .launchIn(coroutineScope)
    }

    override fun observeTokenUpdates(): Flow<String> {
        return firebaseTokenLocalDataSource.observeTokenUpdates()
    }

    override suspend fun getCurrentTokenFromFirebase(): String? {
        return firebaseTokenService.getCurrentToken()
    }
}
