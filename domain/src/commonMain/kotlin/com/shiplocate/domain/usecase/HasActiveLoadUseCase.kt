package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use Case для получения статуса трекинга
 * Проверяет loadStatus в кешированных Loads - если хотя бы один load имеет LOAD_STATUS_CONNECTED, возвращает ACTIVE
 */
class HasActiveLoadUseCase(
    private val loadRepository: LoadRepository,
) {
    suspend operator fun invoke(): Boolean {
        println("🔍 GetTrackingStatusUseCase: Checking tracking status from cached loads...")

        // Получаем кешированные Loads из базы
        val cachedLoads = loadRepository.getCachedLoads()
        println("💾 GetTrackingStatusUseCase: Found ${cachedLoads.size} cached loads")

        // Проверяем, есть ли хотя бы один load с LOAD_STATUS_CONNECTED
        val hasActiveLoad =
            cachedLoads.any { load ->
                val isActive = load.loadStatus == LoadStatus.LOAD_STATUS_CONNECTED
                if (isActive) {
                    println("✅ GetTrackingStatusUseCase: Found active load: ${load.loadName} (status: ${load.loadStatus})")
                }
                isActive
            }

        val status =
            if (hasActiveLoad) {
                println("🟢 GetTrackingStatusUseCase: Tracking is ACTIVE")
                true
            } else {
                println("🔴 GetTrackingStatusUseCase: Tracking is STOPPED")
                false
            }

        return status
    }
}
