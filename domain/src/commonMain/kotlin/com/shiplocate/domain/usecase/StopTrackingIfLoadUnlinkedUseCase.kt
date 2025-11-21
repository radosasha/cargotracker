package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.TrackingRepository

class StopTrackingIfLoadUnlinkedUseCase(
    private val logger: Logger,
    private val loadsRepository: LoadRepository,
    private val trackingRepository: TrackingRepository,
) {

    suspend operator fun invoke(cachedLoads: List<Load>) {
        val connectedLoad = loadsRepository.getConnectedLoad()
        if (connectedLoad != null) {
            val loads = loadsRepository.getCachedLoads()
            val stillInActive = loads.any { it.id == connectedLoad.id && it.loadStatus == LoadStatus.LOAD_STATUS_CONNECTED }
            if (!stillInActive) {
                logger.info(
                    LogCategory.GENERAL,
                    "HandlePushNotificationWhenAppKilledUseCase: Load was IN_TRANSIT, but now it's not, Stop tracking"
                )
                trackingRepository.stopTracking()
            }
        }
    }
}
