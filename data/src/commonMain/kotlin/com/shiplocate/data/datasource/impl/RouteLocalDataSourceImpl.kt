package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.load.RouteLocalDataSource
import com.shiplocate.data.datasource.route.RoutePreferences
import com.shiplocate.data.mapper.toDomain
import com.shiplocate.data.mapper.toDto
import com.shiplocate.data.network.dto.load.RouteDto
import com.shiplocate.domain.model.load.Route
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of RouteLocalDataSource
 * Handles route caching using RoutePreferences
 */
class RouteLocalDataSourceImpl(
    private val routePreferences: RoutePreferences,
    private val json: Json,
) : RouteLocalDataSource {

    override suspend fun saveRoute(
        loadId: Long,
        route: Route,
        provider: String,
        requireUpdate: Boolean,
    ) {
        // Convert Route to RouteDto and serialize to JSON
        val routeDto = route.toDto()
        val routeJson = json.encodeToString(routeDto)

        routePreferences.saveRoute(
            loadId = loadId,
            routeJson = routeJson,
            provider = provider,
            requireUpdate = requireUpdate,
        )
    }

    override suspend fun getRoute(loadId: Long): Route? {
        val savedLoadId = routePreferences.getLoadId()
        if (savedLoadId != loadId) {
            return null
        }

        val routeJson = routePreferences.getRouteJson() ?: return null

        return try {
            // Deserialize JSON to RouteDto and convert to domain model
            val routeDto = json.decodeFromString<RouteDto>(routeJson)
            routeDto.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getRouteProvider(loadId: Long): String? {
        val savedLoadId = routePreferences.getLoadId()
        if (savedLoadId != loadId) {
            return null
        }
        return routePreferences.getProvider()
    }

    override suspend fun deleteRoute(loadId: Long): Boolean {
        val savedLoadId = routePreferences.getLoadId()
        return if (savedLoadId == loadId) {
            routePreferences.clearAll()
        } else false
    }

    override suspend fun clearAllRoutes() {
        routePreferences.clearAll()
    }

    override suspend fun getRequireUpdate(): Boolean {
        return routePreferences.getRequireUpdate()
    }

    override suspend fun setRequireUpdate(requireUpdate: Boolean) {
        routePreferences.saveRequireUpdate(requireUpdate)
    }

    override suspend fun getLoadId(): Long? {
        return routePreferences.getLoadId()
    }
}

