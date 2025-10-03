package com.tracker.domain.service

import com.tracker.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Менеджер для управления синхронизацией неотправленных координат
 * Отдельный сервис, чтобы избежать циклических зависимостей между Use Cases
 */
class LocationSyncService(
    private val locationRepository: LocationRepository,
    private val coroutineScope: CoroutineScope
) {
    
    private var syncJob: Job? = null
    
    companion object Companion {
        // Интервал попыток синхронизации (10 минут)
        private const val SYNC_INTERVAL_MS = 10 * 60 * 1000L
    }
    
    /**
     * Запускает периодическую синхронизацию неотправленных координат
     */
    fun startSync() {
        if (syncJob?.isActive == true) {
            println("LocationSyncManager: Sync already running")
            return
        }
        
        println("LocationSyncManager: Starting periodic sync")
        
        syncJob = coroutineScope.launch {
            while (isActive) {
                try {
                    // Пытаемся отправить неотправленные координаты
                    val result = uploadPendingLocations()
                    
                    if (result.isSuccess) {
                        val count = result.getOrNull() ?: 0
                        if (count > 0) {
                            println("LocationSyncManager: Successfully uploaded $count locations")
                        }
                    } else {
                        println("LocationSyncManager: Failed to upload locations: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    println("LocationSyncManager: Error during sync: ${e.message}")
                }
                
                // Ждем перед следующей попыткой
                delay(SYNC_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Останавливает периодическую синхронизацию
     */
    fun stopSync() {
        println("LocationSyncManager: Stopping sync")
        syncJob?.cancel()
        syncJob = null
    }
    
    /**
     * Проверяет, запущена ли синхронизация
     */
    fun isSyncActive(): Boolean {
        return syncJob?.isActive == true
    }
    
    /**
     * Отправляет все неотправленные координаты на сервер
     */
    private suspend fun uploadPendingLocations(): Result<Int> {
        return try {
            val unsentLocations = locationRepository.getUnsentLocations()
            
            if (unsentLocations.isEmpty()) {
                println("LocationSyncManager: No pending locations to upload")
                return Result.success(0)
            }
            
            println("LocationSyncManager: Found ${unsentLocations.size} pending locations")
            
            var successCount = 0
            val successfulIds = mutableListOf<Long>()
            
            // Отправляем каждую координату
            unsentLocations.forEach { (id, location) ->
                val result = locationRepository.sendLocation(location)
                if (result.isSuccess) {
                    successCount++
                    successfulIds.add(id)
                    println("LocationSyncManager: Location $id uploaded successfully")
                } else {
                    println("LocationSyncManager: Failed to upload location $id: ${result.exceptionOrNull()?.message}")
                }
            }
            
            // Удаляем успешно отправленные
            if (successfulIds.isNotEmpty()) {
                locationRepository.deleteLocationsFromDb(successfulIds)
                println("LocationSyncManager: Deleted $successCount locations from DB")
            }
            
            Result.success(successCount)
        } catch (e: Exception) {
            println("LocationSyncManager: Error uploading pending locations: ${e.message}")
            Result.failure(e)
        }
    }
}
