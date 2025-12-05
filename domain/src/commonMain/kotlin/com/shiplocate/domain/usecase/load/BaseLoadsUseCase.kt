package com.shiplocate.domain.usecase.load

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.Stop
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.RouteRepository

open class BaseLoadsUseCase(
    private val loadRepository: LoadRepository,
    private val authRepository: AuthRepository,
    private val logger: Logger,
    private val routeRepository: RouteRepository,
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

        // Get connected load and its stops from cache before fetching from server
        val cachedConnectedLoad = loadRepository.getConnectedLoad()
        val cachedStops = if (cachedConnectedLoad != null) {
            loadRepository.getStopsByLoadId(cachedConnectedLoad.id)
        } else {
            emptyList()
        }


        val loadsResult = loadRepository.getLoads(token)
        if (loadsResult.isSuccess) {
            val loads = loadsResult.getOrDefault(listOf())

            // Cache the results
            logger.info(LogCategory.GENERAL, "💾 BaseLoadsUseCase: Saving ${loads.size} loads to cache")
            loadRepository.saveLoads(loads)

            // Check if route update is required
            val requireUpdate = routeRepository.getRequireUpdate()
            // Get updated connected load and its stops
            val updatedConnectedLoad = loadRepository.getConnectedLoad()
            val shouldUpdateRoute = when {
                updatedConnectedLoad == null -> false
                requireUpdate -> true
                else -> {
                    val updatedStops = updatedConnectedLoad.stops
                    val stopsChanged = hasStopsChanged(cachedStops, updatedStops)
                    if (stopsChanged) {
                        logger.info(LogCategory.GENERAL, "🔄 BaseLoadsUseCase: Stops changed, route update needed")
                    }
                    stopsChanged
                }
            }

            // Update route if needed
            if (shouldUpdateRoute && updatedConnectedLoad != null) {
                updateRoute(token, updatedConnectedLoad.serverId)
            }

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

    /**
     * Check if stops have changed (new stop, deleted stop, or coordinates changed)
     */
    private fun hasStopsChanged(oldStops: List<Stop>, newStops: List<Stop>): Boolean {
        if (oldStops.size != newStops.size) {
            return true
        }

        val oldStopsMap = oldStops.associateBy { it.id }
        val newStopsMap = newStops.associateBy { it.id }

        // Check if any stop was added or removed
        if (oldStopsMap.keys != newStopsMap.keys) {
            return true
        }

        // Check if coordinates changed for any existing stop
        for ((id, newStop) in newStopsMap) {
            val oldStop = oldStopsMap[id] ?: return true
            if (oldStop.latitude != newStop.latitude || oldStop.longitude != newStop.longitude) {
                return true
            }
        }

        return false
    }

    /**
     * Update route for connected load
     */
    private suspend fun updateRoute(token: String, loadId: Long) {
        logger.info(LogCategory.GENERAL, "🔄 BaseLoadsUseCase: Requesting route for load $loadId")

        val routeResult = loadRepository.getRoute(token, loadId)

        routeResult.fold(
            onSuccess = { route ->
                // Save route to repository (serialization happens in data layer)
                routeRepository.saveRoute(
                    loadId = loadId,
                    route = route,
                    provider = "google",
                    requireUpdate = false,
                )
            },
            onFailure = { error ->
                logger.error(LogCategory.GENERAL, "❌ BaseLoadsUseCase: Failed to get route: ${error.message}", error)
                routeRepository.setRequireUpdate(true)
            }
        )
    }
}
