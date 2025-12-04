package com.shiplocate.domain.usecase.load

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use Case для проверки наличия connected load
 * Сначала проверяет кеш, если не найден - запрашивает с сервера
 */
class GetConnectedLoadUseCase(
    private val loadRepository: LoadRepository,
    authRepository: AuthRepository,
    private val logger: Logger,
): BaseLoadsUseCase(loadRepository, authRepository, logger) {
    suspend operator fun invoke(): Load? {
        logger.info(LogCategory.LOCATION, "HasConnectedLoadUseCase: Checking for connected load...")
        
        // Сначала проверяем кеш
        val cachedConnectedLoad = loadRepository.getConnectedLoad()
        if (cachedConnectedLoad != null) {
            logger.info(
                LogCategory.LOCATION,
                "HasConnectedLoadUseCase: Found connected load in cache: ${cachedConnectedLoad.loadName}",
            )
            return cachedConnectedLoad
        }
        
        logger.info(LogCategory.LOCATION, "HasConnectedLoadUseCase: No connected load in cache, checking server...")

        // Получаем loads с сервера (с fallback на кеш)
        val loadsResult = getLoads()
        
        return if (loadsResult.isSuccess) {
            val loads = loadsResult.getOrNull() ?: emptyList()
            val hasConnectedLoad = loads.find { it.loadStatus == LoadStatus.LOAD_STATUS_CONNECTED }
            
            if (hasConnectedLoad != null) {
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
            null
        }
    }
}

