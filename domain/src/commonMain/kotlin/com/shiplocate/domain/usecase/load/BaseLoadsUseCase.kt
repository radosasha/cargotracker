package com.shiplocate.domain.usecase.load

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository

open class BaseLoadsUseCase(
    private val loadRepository: LoadRepository,
    private val authRepository: AuthRepository,
    private val logger: Logger,
) {
    /**
     * Get loads from server or cache
     * Automatically retrieves auth token from preferences
     * @return Result with list of loads
     */
    open suspend fun getLoads(): Result<List<Load>> {
        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            return Result.failure(Exception("Not authenticated"))
        }

        val loadsResult = loadRepository.getLoads(token)
        if (loadsResult.isSuccess) {
            val loads = loadsResult.getOrDefault(listOf())

            // Cache the results
            logger.info(LogCategory.GENERAL, "💾 BaseLoadsUseCase: Saving ${loads.size} loads to cache")
            loadRepository.saveLoads(loads)

            val cachedLoads = loadRepository.getCachedLoads()
            logger.info(LogCategory.GENERAL, "✅ BaseLoadsUseCase: Successfully loaded ${cachedLoads.size} loads from server")
            return Result.success(cachedLoads)
        } else {
            val cachedLoads = loadRepository.getCachedLoads()
            return if (cachedLoads.isNotEmpty()) {
                logger.info(LogCategory.GENERAL, "✅ BaseLoadsUseCase: Loaded ${cachedLoads.size} loads from cache")
                Result.success(cachedLoads)
            } else {
                logger.info(LogCategory.GENERAL, "❌ BaseLoadsUseCase: No cached loads available")
                Result.failure(Exception("No cached data available. Please check your connection."))
            }
        }
    }
}
