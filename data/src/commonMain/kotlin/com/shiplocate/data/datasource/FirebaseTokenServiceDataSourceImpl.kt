package com.shiplocate.data.datasource

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Реализация FirebaseTokenServiceDataSource
 * Обрабатывает получение токенов от Firebase и уведомления о новых токенах
 */
open class FirebaseTokenServiceDataSourceImpl : FirebaseTokenServiceDataSource {
    private val coroutineScope = MainScope()

    // Flow для уведомления о новых токенах
    private val _newTokenFlow = MutableSharedFlow<String>()

    override suspend fun getCurrentToken(): String? {
        // Платформо-специфичная реализация будет в expect/actual
        return getCurrentTokenFromPlatform()
    }

    override fun getNewTokenFlow(): Flow<String> {
        return _newTokenFlow.asSharedFlow()
    }

    override fun onNewTokenReceived(token: String) {
        coroutineScope.launch {
            println("FirebaseTokenServiceDataSource: New token received: ${token.take(20)}...")
            _newTokenFlow.emit(token)
        }
    }

    override fun onPushNotificationReceived(userInfo: Map<String, Any>) {
        println("FirebaseTokenServiceDataSource: Push notification received: $userInfo")
        // TODO: Implement push notification handling
    }

    /**
     * Платформо-специфичный метод для получения текущего токена
     * Будет реализован в expect/actual
     */
    private suspend fun getCurrentTokenFromPlatform(): String? {
        // Пока возвращаем null, так как платформо-специфичная реализация в composeApp модуле
        return null
    }
}
