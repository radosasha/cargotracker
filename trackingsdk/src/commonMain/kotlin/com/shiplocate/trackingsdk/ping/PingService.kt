package com.shiplocate.trackingsdk.ping

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Service for sending periodic ping requests to server
 * Updates connection status every 10 minutes
 */
class PingService(
    private val pingIntervalMs: Long,
    private val authRepository: AuthRepository,
    private val loadRepository: LoadRepository,
    private val logger: Logger,
    private val scope: CoroutineScope,
) {
    private var pingJob: Job? = null

    /**
     * Start periodic ping requests
     * Sends ping immediately, then every 10 minutes to update connection status
     */
    fun start() {
        if (pingJob?.isActive == true) {
            logger.debug(LogCategory.NETWORK, "PingService: Already running")
            return
        }

        logger.info(LogCategory.NETWORK, "PingService: Starting periodic ping (every 10 minutes)")
        pingJob = scope.launch {
            // Send first ping immediately
            try {
                sendPing()
            } catch (e: Exception) {
                logger.error(LogCategory.NETWORK, "PingService: Error sending initial ping: ${e.message}", e)
            }

            // Then send ping every 10 minutes
            while (isActive) {
                delay(pingIntervalMs)
                try {
                    sendPing()
                } catch (e: Exception) {
                    logger.error(LogCategory.NETWORK, "PingService: Error sending ping: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Stop periodic ping requests
     */
    fun stop() {
        logger.info(LogCategory.NETWORK, "PingService: Stopping periodic ping")
        pingJob?.cancel()
        pingJob = null
    }

    private suspend fun sendPing() {
        // Get authentication token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            logger.warn(LogCategory.NETWORK, "PingService: No authentication token available, skipping ping")
            return
        }

        // Get connected load
        val connectedLoad = loadRepository.getConnectedLoad()
        if (connectedLoad == null) {
            logger.warn(LogCategory.NETWORK, "PingService: No connected load found, skipping ping")
            return
        }

        logger.debug(LogCategory.NETWORK, "PingService: Sending ping for load ${connectedLoad.loadName} (serverId: ${connectedLoad.serverId})")

        try {
            val result = loadRepository.pingLoad(token, connectedLoad.serverId)
            result.getOrThrow()
            logger.info(LogCategory.NETWORK, "PingService: ✅ Ping sent successfully for load ${connectedLoad.loadName} (serverId: ${connectedLoad.serverId})")
        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "PingService: ❌ Failed to send ping: ${e.message}", e)
            throw e
        }
    }
}

