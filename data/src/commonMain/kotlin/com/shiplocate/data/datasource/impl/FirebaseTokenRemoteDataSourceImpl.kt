package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.FirebaseTokenRemoteDataSource
import com.shiplocate.data.network.api.FirebaseTokenApi
import com.shiplocate.data.network.dto.firebase.FirebaseTokenRequestDto
import com.shiplocate.domain.repository.AuthPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Реализация FirebaseTokenRemoteDataSource
 * Отправляет токены на сервер через FirebaseTokenApi
 */
class FirebaseTokenRemoteDataSourceImpl(
    private val firebaseTokenApi: FirebaseTokenApi,
    private val authPreferencesRepository: AuthPreferencesRepository,
) : FirebaseTokenRemoteDataSource {

    private val _pushedFlow = MutableSharedFlow<Int?>(replay = 0)
    override suspend fun sendTokenToServer(token: String) {
        println("Sending Firebase token to server: $token")

        try {
            // Получаем токен авторизации из сессии
            val authSession = authPreferencesRepository.getSession()
            if (authSession == null) {
                println("No auth session found, cannot send Firebase token to server")
                return
            }

            val request = FirebaseTokenRequestDto(firebaseToken = token)
            val response = firebaseTokenApi.updateFirebaseToken(authSession.token, request)

            if (response.success) {
                println("Token sent to server successfully: ${response.message}")
            } else {
                println("Failed to send token to server: ${response.message}")
            }
        } catch (e: Exception) {
            println("Error sending token to server: ${e.message}")
            throw e
        }
    }

    override suspend fun clearToken() {
        try {
            val authSession = authPreferencesRepository.getSession()
            if (authSession == null) {
                println("No auth session found, cannot clear token")
                return
            }

            val response = firebaseTokenApi.clearFirebaseToken(authSession.token)
            if (response.success) {
                println("Token cleared successfully: ${response.message}")
            } else {
                println("Failed to clear token: ${response.message}")
            }
        } catch (e: Exception) {
            println("Error clearing token: ${e.message}")
            throw e
        }
    }

    override suspend fun pushReceived(type: Int?) {
        _pushedFlow.emit(type)
    }

    override fun observeReceivedPushes(): Flow<Int?> {
        return _pushedFlow
    }
}
