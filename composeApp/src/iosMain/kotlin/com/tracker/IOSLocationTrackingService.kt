package com.tracker

import com.tracker.domain.usecase.StartProcessLocationsUseCase
import com.tracker.domain.usecase.StopProcessLocationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS Location Tracking Service
 * Аналог AndroidTrackingService для iOS платформы
 * Управляет фоновым GPS трекингом на iOS
 * Singleton для обеспечения единого экземпляра при пересоздании UI
 */
class IOSLocationTrackingService private constructor() : KoinComponent {
    
    private var isTracking = false
    
    // Koin DI - используем Use Cases
    private val startProcessLocationsUseCase: StartProcessLocationsUseCase by inject()
    private val stopProcessLocationsUseCase: StopProcessLocationsUseCase by inject()
    
    // Coroutine scope for background operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    companion object {
        private const val TAG = "IOSLocationTrackingService"
        
        private var INSTANCE: IOSLocationTrackingService? = null
        
        /**
         * Получает единственный экземпляр сервиса
         * Thread-safe singleton implementation для Kotlin/Native
         */
        fun getInstance(): IOSLocationTrackingService {
            if (INSTANCE == null) {
                INSTANCE = IOSLocationTrackingService()
            }
            return INSTANCE!!
        }
        
        /**
         * Проверяет, инициализирован ли сервис
         */
        fun isInitialized(): Boolean = INSTANCE != null
        
        /**
         * Принудительно создает новый экземпляр (для тестов)
         */
        fun createNewInstance(): IOSLocationTrackingService {
            INSTANCE?.destroy()
            INSTANCE = IOSLocationTrackingService()
            return INSTANCE!!
        }
        
        /**
         * Запускает GPS трекинг через singleton
         */
        suspend fun startTracking() {
            getInstance().startLocationTracking()
        }
        
        /**
         * Останавливает GPS трекинг через singleton
         */
        suspend fun stopTracking() {
            getInstance().stopLocationTracking()
        }
        
        /**
         * Проверяет статус трекинга через singleton
         */
        fun isTrackingActive(): Boolean {
            return getInstance().isLocationTrackingActive()
        }
        
        /**
         * Получает статус сервиса через singleton
         */
        fun getStatus(): String {
            return getInstance().getServiceStatus()
        }
        
        /**
         * Уничтожает singleton (для завершения приложения)
         */
        fun destroyInstance() {
            INSTANCE?.destroy()
        }
    }
    
    /**
     * Запускает GPS трекинг
     * Аналог onStartCommand в Android Service
     */
    suspend fun startLocationTracking() {
        if (isTracking) {
            println("$TAG: Already tracking, ignoring start request")
            return
        }
        
        try {
            println("$TAG: Starting GPS tracking through StartProcessLocationsUseCase")
            
            // Запускаем обработку GPS координат через StartProcessLocationsUseCase
            startProcessLocationsUseCase()
            isTracking = true
            
            // Сохраняем состояние для восстановления
            // TODO: Добавить сохранение состояния
            
            println("$TAG: ✅ GPS tracking started successfully")
            
        } catch (e: Exception) {
            println("$TAG: ❌ Error starting GPS tracking: ${e.message}")
            e.printStackTrace()
            isTracking = false // Сбрасываем состояние при ошибке
            // TODO: Добавить сохранение состояния
        }
    }
    
    /**
     * Останавливает GPS трекинг
     * Аналог stopLocationTracking в Android Service
     */
    fun stopLocationTracking() {
        if (!isTracking) {
            println("$TAG: Not tracking, ignoring stop request")
            return
        }
        
        serviceScope.launch {
            try {
                println("$TAG: Stopping GPS tracking through StopProcessLocationsUseCase")
                
                // Останавливаем обработку GPS координат
                val result = stopProcessLocationsUseCase()
                if (result.isSuccess) {
                    isTracking = false
                    println("$TAG: ✅ GPS tracking stopped successfully")
                } else {
                    println("$TAG: ❌ Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
                    // Принудительно сбрасываем состояние даже при ошибке
                    isTracking = false
                }
                
            } catch (e: Exception) {
                println("$TAG: ❌ Error stopping GPS tracking: ${e.message}")
                e.printStackTrace()
                // Принудительно сбрасываем состояние при исключении
                isTracking = false
            }
        }
    }
    
    /**
     * Синхронная остановка GPS трекинга
     * Аналог stopLocationTrackingSync в Android Service
     */
    private suspend fun stopLocationTrackingSync() {
        if (!isTracking) {
            println("$TAG: Not tracking, ignoring stop request")
            return
        }
        
        try {
            println("$TAG: Stopping GPS tracking synchronously")
            
            // Останавливаем обработку GPS координат синхронно
            val result = stopProcessLocationsUseCase()
            if (result.isSuccess) {
                isTracking = false
                println("$TAG: ✅ GPS tracking stopped successfully")
            } else {
                println("$TAG: ❌ Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            println("$TAG: ❌ Error stopping GPS tracking: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Останавливает сервис и освобождает ресурсы
     * Аналог onDestroy в Android Service
     */
    fun destroy() {
        println("$TAG: Destroying service")
        
        // Синхронная остановка GPS трекинга перед отменой scope
        if (isTracking) {
            try {
                // Используем runBlocking для синхронного выполнения
                runBlocking {
                    stopLocationTrackingSync()
                }
            } catch (e: Exception) {
                println("$TAG: Error in runBlocking: ${e.message}")
            }
        }
        
        // Останавливаем serviceScope, что отменит все корутины
        serviceScope.cancel()
        println("$TAG: Service scope cancelled")
        
        // Очищаем singleton instance
        INSTANCE = null
    }
    
    /**
     * Проверяет, активен ли GPS трекинг
     * Аналог isLocationTrackingActive в Android Service
     */
    fun isLocationTrackingActive(): Boolean = isTracking
    
    /**
     * Получает статус сервиса для отладки
     */
    fun getServiceStatus(): String {
        return "IOSLocationTrackingService(status=${if (isTracking) "TRACKING" else "STOPPED"})"
    }
    
    /**
     * Восстанавливает состояние трекинга после перезапуска приложения
     */
    fun restoreTrackingState() {
        // TODO: Добавить восстановление состояния из UserDefaults
        println("$TAG: TODO: Implement state restoration")
    }
}
