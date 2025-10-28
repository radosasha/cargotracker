package com.shiplocate.trackingsdk

/**
 * Интерфейс TrackingSDK
 * Определяет публичный API для SDK
 */
interface TrackingSDK {
    suspend fun startTracking(): Result<Unit>
    suspend fun stopTracking(): Result<Unit>
    suspend fun isTrackingActive(): Boolean
    fun getServiceStatus(): String
    fun destroy() // Метод для очистки ресурсов
}

/**
 * Внутренняя фабрика для получения TrackingSDK singleton экземпляра
 * Используется только внутри модуля trackingsdk
 * Singleton управляется через Koin DI модуль
 */
internal object TrackingSDKFactory {
    
    private var instance: TrackingSDK? = null
    
    /**
     * Получает единственный экземпляр TrackingSDK
     * Экземпляр должен быть зарегистрирован в Koin через trackingSDKModule
     */
    fun getInstance(): TrackingSDK {
        return instance ?: throw IllegalStateException(
            "TrackingSDK not initialized. Make sure trackingSDKModule is loaded in Koin."
        )
    }
    
    /**
     * Внутренний метод для установки экземпляра (вызывается из DI модуля)
     */
    internal fun setInstance(sdk: TrackingSDK) {
        instance = sdk
    }
    
    /**
     * Очищает singleton экземпляр (для тестирования)
     */
    fun clearInstance() {
        instance?.destroy()
        instance = null
    }
    
    /**
     * Проверяет, инициализирован ли singleton
     */
    fun isInitialized(): Boolean = instance != null
}
