package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.route.RoutePreferences
import com.shiplocate.data.mapper.toDomain
import com.shiplocate.data.mapper.toDto
import com.shiplocate.data.network.dto.load.RouteDto
import com.shiplocate.domain.model.load.Route
import com.shiplocate.domain.repository.RouteRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of RouteRepository
 * Handles route caching using RoutePreferences
 */
class RouteRepositoryImpl(
    private val routePreferences: RoutePreferences,
    private val json: Json,
    private val logger: Logger,
) : RouteRepository {

    override suspend fun getRequireUpdate(): Boolean {
        return routePreferences.getRequireUpdate()
    }

    override suspend fun saveRoute(
        loadId: Long,
        route: Route,
        provider: String,
        requireUpdate: Boolean,
    ) {
        // Convert Route to RouteDto and serialize to JSON
        val routeDto = route.toDto()
        val routeJson = json.encodeToString(routeDto)

        // Save route to preferences
        routePreferences.saveRoute(
            loadId = loadId,
            routeJson = routeJson,
            provider = provider,
            requireUpdate = requireUpdate,
        )

        logger.info(LogCategory.GENERAL, "✅ RouteRepositoryImpl: Successfully saved route for load $loadId")
    }

    override suspend fun setRequireUpdate(requireUpdate: Boolean) {
        routePreferences.saveRequireUpdate(requireUpdate)
    }

    override suspend fun getRoute(loadId: Long): Route? {
        return try {
            val savedLoadId = routePreferences.getLoadId()
            if (savedLoadId != loadId) {
                logger.debug(LogCategory.GENERAL, "RouteRepositoryImpl: Route loadId ($savedLoadId) doesn't match requested loadId ($loadId)")
                return null
            }

            val routeJson = routePreferences.getRouteJson()
            if (routeJson == null) {
                logger.debug(LogCategory.GENERAL, "RouteRepositoryImpl: No route JSON found for load $loadId")
                return null
            }

            // Deserialize JSON to RouteDto and convert to domain model
            val routeDto = json.decodeFromString<RouteDto>(routeJson)
            val route = routeDto.toDomain()

            logger.debug(LogCategory.GENERAL, "✅ RouteRepositoryImpl: Successfully retrieved route for load $loadId")
            route
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "❌ RouteRepositoryImpl: Failed to deserialize route: ${e.message}", e)
            null
        }
    }
}

