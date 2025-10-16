package com.tracker.domain.usecase.load

import com.tracker.domain.model.load.Load
import com.tracker.domain.repository.LoadRepository

/**
 * Use case to get cached loads from local database
 * Used when returning from other screens to refresh the list with latest cached data
 */
class GetCachedLoadsUseCase(
    private val loadRepository: LoadRepository
) {
    /**
     * Get cached loads from local database
     * @return List of cached loads
     */
    suspend operator fun invoke(): List<Load> {
        println("💾 GetCachedLoadsUseCase: Getting cached loads")
        return loadRepository.getCachedLoads()
    }
}




