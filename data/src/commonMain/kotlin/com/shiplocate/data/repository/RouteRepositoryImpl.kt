package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.load.LoadsRemoteDataSource
import com.shiplocate.data.datasource.load.RouteLocalDataSource
import com.shiplocate.data.mapper.toDomain
import com.shiplocate.domain.model.load.Route
import com.shiplocate.domain.repository.RouteRepository

/**
 * Implementation of RouteRepository
 * Handles route caching using RouteLocalDataSource
 */
class RouteRepositoryImpl(
    private val routeLocalDataSource: RouteLocalDataSource,
    private val loadsRemoteDataSource: LoadsRemoteDataSource,
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

    override suspend fun getCachedRoute(loadId: Long): Route? {
        return routeLocalDataSource.getRoute(loadId)
    }

    override suspend fun deleteRoute(loadId: Long) {
        val deleted = routeLocalDataSource.deleteRoute(loadId)
        logger.info(LogCategory.GENERAL, "🧹 RouteRepositoryImpl: Deleted cached route for load $loadId, deleted=$deleted")
    }

    override suspend fun getRoute(
        token: String,
        serverLoadId: Long,
    ): Result<Route> {
        logger.info(LogCategory.GENERAL, "🔄 RouteRepositoryImpl: Getting route for load $serverLoadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 RouteRepositoryImpl: Sending route request to server")
            val routeDto = loadsRemoteDataSource.getRoute(token, serverLoadId)
            val route = routeDto.toDomain()
            logger.info(LogCategory.GENERAL, "✅ RouteRepositoryImpl: Successfully got route for load $serverLoadId")
            Result.success(route)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ RouteRepositoryImpl: Failed to get route for load $serverLoadId: ${e.message}")
            Result.failure(e)
        }
    }
}

