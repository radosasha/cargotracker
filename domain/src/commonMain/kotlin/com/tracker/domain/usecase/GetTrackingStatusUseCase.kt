package com.tracker.domain.usecase

import com.tracker.domain.model.TrackingStatus
import com.tracker.domain.repository.LoadRepository

/**
 * Use Case для получения статуса трекинга
 * Проверяет loadStatus в кешированных Loads - если хотя бы один load имеет loadStatus == 1, возвращает ACTIVE
 */
class GetTrackingStatusUseCase(
    private val loadRepository: LoadRepository,
) {
    suspend operator fun invoke(): TrackingStatus {
        println("🔍 GetTrackingStatusUseCase: Checking tracking status from cached loads...")

        try {
            // Получаем кешированные Loads из базы
            val cachedLoads = loadRepository.getCachedLoads()
            println("💾 GetTrackingStatusUseCase: Found ${cachedLoads.size} cached loads")

            // Проверяем, есть ли хотя бы один load с loadStatus == 1 (Connected)
            val hasActiveLoad =
                cachedLoads.any { load ->
                    val isActive = load.loadStatus == 1
                    if (isActive) {
                        println("✅ GetTrackingStatusUseCase: Found active load: ${load.loadId} (status: ${load.loadStatus})")
                    }
                    isActive
                }

            val status =
                if (hasActiveLoad) {
                    println("🟢 GetTrackingStatusUseCase: Tracking is ACTIVE")
                    TrackingStatus.ACTIVE
                } else {
                    println("🔴 GetTrackingStatusUseCase: Tracking is STOPPED")
                    TrackingStatus.STOPPED
                }

            return status
        } catch (e: Exception) {
            println("❌ GetTrackingStatusUseCase: Error reading cached loads: ${e.message}")
            return TrackingStatus.STOPPED
        }
    }
}
