package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для работы с Firebase токенами
 * Реализуется в composeApp модуле через expect/actual классы
 */
interface FirebaseTokenService {
    
    /**
     * Получить текущий Firebase токен
     */
    suspend fun getCurrentToken(): String?
    
    /**
     * Flow для получения новых токенов
     */
    fun getNewTokenFlow(): Flow<String>
    
    /**
     * Обработка нового токена (вызывается системой)
     */
    fun onNewTokenReceived(token: String)
    
    /**
     * Обработка пуш-уведомления (вызывается системой)
     */
    fun onPushNotificationReceived(userInfo: Map<String, Any>)
}
