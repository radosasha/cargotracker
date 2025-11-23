package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Expect класс для работы с Firebase токенами
 * Платформо-специфичные реализации в androidMain и iosMain
 */
expect class FirebaseTokenServiceDataSource {
    
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
    suspend fun onNewTokenReceived(token: String)
}
