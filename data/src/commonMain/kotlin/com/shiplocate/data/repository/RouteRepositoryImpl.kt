package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.load.RouteLocalDataSource
import com.shiplocate.domain.model.load.Route
import com.shiplocate.domain.repository.RouteRepository

/**
 * Implementation of RouteRepository
 * Handles route caching using RouteLocalDataSource
 */
class RouteRepositoryImpl(
    private val routeLocalDataSource: RouteLocalDataSource,
    private val logger: Logger,
) : RouteRepository {

    override suspend fun getRequireUpdate(): Boolean {
        return routeLocalDataSource.getRequireUpdate()
    }

    override suspend fun saveRoute(
        loadId: Long,
        route: Route,
        provider: String,
        requireUpdate: Boolean,
    ) {
        routeLocalDataSource.saveRoute(
            loadId = loadId,
            route = route,
            provider = provider,
            requireUpdate = requireUpdate,
        )

        logger.info(LogCategory.GENERAL, "✅ RouteRepositoryImpl: Successfully saved route for load $loadId")
    }

    override suspend fun setRequireUpdate(requireUpdate: Boolean) {
        routeLocalDataSource.setRequireUpdate(requireUpdate)
    }

    override suspend fun getRoute(loadId: Long): Route? {
        return routeLocalDataSource.getRoute(loadId)
    }
}

