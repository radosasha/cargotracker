package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use Case для получения статуса трекинга
 * Проверяет loadStatus в кешированных Loads - если хотя бы один load имеет LOAD_STATUS_CONNECTED, возвращает ACTIVE
 */
class GetActiveLoadUseCase(
    private val loadRepository: LoadRepository,
) {
    suspend operator fun invoke(): Load? {
        println("🔍 GetTrackingStatusUseCase: Checking tracking status from cached loads...")

        // Получаем кешированные Loads из базы
        val cachedLoads = loadRepository.getCachedLoads()
        println("💾 GetTrackingStatusUseCase: Found ${cachedLoads.size} cached loads")

        // Проверяем, есть ли хотя бы один load с LOAD_STATUS_CONNECTED
        return cachedLoads.find { load ->
             load.loadStatus == LoadStatus.LOAD_STATUS_CONNECTED
        }
    }
}
