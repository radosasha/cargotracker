package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Service DataSource для работы с Firebase токенами
 * Отвечает за получение токенов от Firebase и уведомления о новых токенах
 */
interface FirebaseTokenServiceDataSource {
    suspend fun getCurrentToken(): String?

    fun getNewTokenFlow(): Flow<String>

    fun onNewTokenReceived(token: String)

    fun onPushNotificationReceived(userInfo: Map<String, Any>)
}
