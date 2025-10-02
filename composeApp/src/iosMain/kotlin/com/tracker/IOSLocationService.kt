package com.tracker

import com.tracker.domain.model.Location
import com.tracker.domain.usecase.ProcessLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS сервис для обработки GPS координат
 * Эквивалент LocationTrackingService для iOS
 */
class IOSLocationService : KoinComponent {
    
    private val processLocationUseCase: ProcessLocationUseCase by inject()
    private val locationManager: IOSLocationManager by inject()
    
    // Coroutine scope for processing operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var isProcessing = false
    
    /**
     * Запускает обработку GPS координат
     */
    fun startLocationProcessing(): Result<Unit> {
        return try {
            if (isProcessing) {
                return Result.success(Unit) // Уже обрабатываем
            }
            
            // Запускаем GPS трекинг
            val trackingResult = locationManager.startLocationTracking()
            if (trackingResult.isFailure) {
                return trackingResult
            }
            
            // Подписываемся на GPS координаты
            serviceScope.launch {
                locationManager.observeLocationUpdates().collect { location ->
                    processLocation(location)
                }
            }
            
            isProcessing = true
            println("IOSLocationService: Location processing started")
            Result.success(Unit)
        } catch (e: Exception) {
            println("IOSLocationService: Failed to start location processing: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Останавливает обработку GPS координат
     */
    fun stopLocationProcessing(): Result<Unit> {
        return try {
            if (!isProcessing) {
                return Result.success(Unit) // Уже остановлено
            }
            
            // Останавливаем GPS трекинг
            locationManager.stopLocationTracking()
            isProcessing = false
            
            println("IOSLocationService: Location processing stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            println("IOSLocationService: Failed to stop location processing: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Проверяет, активна ли обработка координат
     */
    fun isLocationProcessingActive(): Boolean = isProcessing
    
    /**
     * Обрабатывает GPS координату
     */
    private suspend fun processLocation(location: Location) {
        try {
            println("IOSLocationService: Processing location: ${location.latitude}, ${location.longitude}")
            
            val result = processLocationUseCase(location)
            
            if (result.shouldSend) {
                println("IOSLocationService: ✅ Successfully processed location")
                println("IOSLocationService: Reason: ${result.reason}")
            } else {
                println("IOSLocationService: ⏭️ Location filtered out")
                println("IOSLocationService: Reason: ${result.reason}")
            }
            
            println("IOSLocationService: Total sent: ${result.totalSent}, Total received: ${result.totalReceived}")
            
        } catch (e: Exception) {
            println("IOSLocationService: ❌ Error processing location: ${e.message}")
            e.printStackTrace()
        }
    }
}
