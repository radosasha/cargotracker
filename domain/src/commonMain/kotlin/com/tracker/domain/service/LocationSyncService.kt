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
     * Использует пакетную отправку для эффективности
     * Обрабатывает большие списки пакетами для избежания проблем с памятью и сетью
     */
    private suspend fun uploadPendingLocations(): Result<Int> {
        return try {
            val unsentLocations = locationRepository.getUnsentLocations()
            
            if (unsentLocations.isEmpty()) {
                println("LocationSyncManager: No pending locations to upload")
                return Result.success(0)
            }
            
            println("LocationSyncManager: Found ${unsentLocations.size} pending locations")
            
            // Максимальный размер пакета для отправки (1000 координат)
            val maxBatchSize = 100
            var totalUploaded = 0
            
            if (unsentLocations.size <= maxBatchSize) {
                // Небольшой список - отправляем целиком
                val locations = unsentLocations.map { it.second }
                val result = locationRepository.sendLocations(locations)
                
                if (result.isSuccess) {
                    val allIds = unsentLocations.map { it.first }
                    locationRepository.deleteLocationsFromDb(allIds)
                    println("LocationSyncManager: Successfully uploaded ${unsentLocations.size} locations and deleted from DB")
                    totalUploaded = unsentLocations.size
                } else {
                    println("LocationSyncManager: Failed to upload locations: ${result.exceptionOrNull()?.message}")
                    return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } else {
                // Большой список - обрабатываем пакетами
                println("LocationSyncManager: Large dataset detected (${unsentLocations.size} locations), processing in batches of $maxBatchSize")
                
                val batches = unsentLocations.chunked(maxBatchSize)
                val allSuccessfulIds = mutableListOf<Long>()
                
                batches.forEachIndexed { index, batch ->
                    println("LocationSyncManager: Processing batch ${index + 1}/${batches.size} (${batch.size} locations)")
                    
                    val locations = batch.map { it.second }
                    val result = locationRepository.sendLocations(locations)
                    
                    if (result.isSuccess) {
                        val batchIds = batch.map { it.first }
                        allSuccessfulIds.addAll(batchIds)
                        totalUploaded += batch.size
                        println("LocationSyncManager: Batch ${index + 1} uploaded successfully (${batch.size} locations)")
                    } else {
                        println("LocationSyncManager: Batch ${index + 1} failed: ${result.exceptionOrNull()?.message}")
                        // Продолжаем с остальными пакетами даже если один не удался
                    }
                }
                
                // Удаляем все успешно отправленные координаты
                if (allSuccessfulIds.isNotEmpty()) {
                    locationRepository.deleteLocationsFromDb(allSuccessfulIds)
                    println("LocationSyncManager: Deleted $totalUploaded locations from DB")
                }
            }
            
            Result.success(totalUploaded)
        } catch (e: Exception) {
            println("LocationSyncManager: Error uploading pending locations: ${e.message}")
            Result.failure(e)
        }
    }
}
