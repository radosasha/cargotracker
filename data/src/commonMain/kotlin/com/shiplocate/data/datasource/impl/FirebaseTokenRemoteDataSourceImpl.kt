package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
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
    private val logger: Logger,
) : FirebaseTokenRemoteDataSource {

    private val _pushedFlow = MutableSharedFlow<Int?>(replay = 0)
    override suspend fun sendTokenToServer(token: String) {
        logger.info(LogCategory.NETWORK, "FirebaseTokenRemoteDataSource: Sending Firebase token to server")

        try {
            // Получаем токен авторизации из сессии
            val authSession = authPreferencesRepository.getSession()
            if (authSession == null) {
                logger.warn(LogCategory.AUTH, "FirebaseTokenRemoteDataSource: No auth session, cannot send token")
                return
            }

            val request = FirebaseTokenRequestDto(firebaseToken = token)
            val response = firebaseTokenApi.updateFirebaseToken(authSession.token, request)

            if (response.success) {
                logger.info(LogCategory.NETWORK, "FirebaseTokenRemoteDataSource: Token sent successfully")
            } else {
                logger.warn(LogCategory.NETWORK, "FirebaseTokenRemoteDataSource: Failed to send token: ${response.message}")
            }
        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "FirebaseTokenRemoteDataSource: Error sending token", e)
            throw e
        }
    }

    override suspend fun clearToken() {
        try {
            val authSession = authPreferencesRepository.getSession()
            if (authSession == null) {
                logger.warn(LogCategory.AUTH, "FirebaseTokenRemoteDataSource: No auth session, cannot clear token")
                return
            }

            val response = firebaseTokenApi.clearFirebaseToken(authSession.token)
            if (response.success) {
                logger.info(LogCategory.NETWORK, "FirebaseTokenRemoteDataSource: Token cleared successfully")
            } else {
                logger.warn(LogCategory.NETWORK, "FirebaseTokenRemoteDataSource: Failed to clear token: ${response.message}")
            }
        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "FirebaseTokenRemoteDataSource: Error clearing token", e)
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
