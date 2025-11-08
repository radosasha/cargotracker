package com.shiplocate.domain.usecase.load

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use Case для проверки наличия connected load
 * Сначала проверяет кеш, если не найден - запрашивает с сервера
 */
class HasConnectedLoadUseCase(
    private val loadRepository: LoadRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val logger: Logger,
) {
    suspend operator fun invoke(): Boolean {
        logger.info(LogCategory.LOCATION, "HasConnectedLoadUseCase: Checking for connected load...")
        
        // Сначала проверяем кеш
        val cachedConnectedLoad = loadRepository.getConnectedLoad()
        if (cachedConnectedLoad != null) {
            logger.info(
                LogCategory.LOCATION,
                "HasConnectedLoadUseCase: Found connected load in cache: ${cachedConnectedLoad.loadName}",
            )
            return true
        }
        
        logger.info(LogCategory.LOCATION, "HasConnectedLoadUseCase: No connected load in cache, checking server...")
        
        // Если в кеше нет, получаем token и запрашиваем с сервера
        val authSession = authPreferencesRepository.getSession()
        val token = authSession?.token
        
        if (token == null) {
            logger.warn(LogCategory.LOCATION, "HasConnectedLoadUseCase: Not authenticated, cannot check server")
            return false
        }
        
        // Получаем loads с сервера (с fallback на кеш)
        val loadsResult = loadRepository.getLoads(token)
        
        return if (loadsResult.isSuccess) {
            val loads = loadsResult.getOrNull() ?: emptyList()
            val hasConnectedLoad = loads.any { it.loadStatus == 1 }
            
            if (hasConnectedLoad) {
                logger.info(
                    LogCategory.LOCATION,
                    "HasConnectedLoadUseCase: Found connected load on server",
                )
            } else {
                logger.info(
                    LogCategory.LOCATION,
                    "HasConnectedLoadUseCase: No connected load found on server",
                )
            }
            
            hasConnectedLoad
        } else {
            logger.warn(
                LogCategory.LOCATION,
                "HasConnectedLoadUseCase: Failed to get loads from server: ${loadsResult.exceptionOrNull()?.message}",
            )
            false
        }
    }
}

