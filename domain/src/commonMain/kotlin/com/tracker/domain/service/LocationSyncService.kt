package com.tracker.domain.service

import com.tracker.domain.usecase.UploadPendingLocationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Сервис для периодической синхронизации неотправленных координат с сервером
 */
class LocationSyncService(
    private val uploadPendingLocationsUseCase: UploadPendingLocationsUseCase,
    private val coroutineScope: CoroutineScope
) {
    
    private var syncJob: Job? = null
    
    companion object {
        // Интервал попыток синхронизации (5 минут)
        private const val SYNC_INTERVAL_MS = 5 * 60 * 1000L
    }
    
    /**
     * Запускает периодическую синхронизацию
     */
    fun startSync() {
        if (syncJob?.isActive == true) {
            println("LocationSyncService: Sync already running")
            return
        }
        
        println("LocationSyncService: Starting periodic sync")
        
        syncJob = coroutineScope.launch {
            while (isActive) {
                try {
                    // Пытаемся отправить неотправленные координаты
                    val result = uploadPendingLocationsUseCase()
                    
                    if (result.isSuccess) {
                        val count = result.getOrNull() ?: 0
                        if (count > 0) {
                            println("LocationSyncService: Successfully uploaded $count locations")
                        }
                    } else {
                        println("LocationSyncService: Failed to upload locations: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    println("LocationSyncService: Error during sync: ${e.message}")
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
        println("LocationSyncService: Stopping sync")
        syncJob?.cancel()
        syncJob = null
    }
    
    /**
     * Проверяет, запущена ли синхронизация
     */
    fun isSyncActive(): Boolean {
        return syncJob?.isActive == true
    }
}

