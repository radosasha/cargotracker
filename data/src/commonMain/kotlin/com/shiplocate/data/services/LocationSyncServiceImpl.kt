package com.shiplocate.data.services

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.LocationRepository
import com.shiplocate.domain.service.LocationSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Реализация LocationSyncService в data слое
 * Управляет синхронизацией неотправленных координат с сервером
 *
 * Находится в data слое, так как:
 * - Содержит mutable state (syncJob)
 * - Зависит от infrastructure (CoroutineScope, Job)
 * - Управляет lifecycle операций
 */
class LocationSyncServiceImpl(
    private val locationRepository: LocationRepository,
    private val loadRepository: LoadRepository,
    private val authPrefsRepository: AuthPreferencesRepository,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger,
) : LocationSyncService {
    private var syncJob: Job? = null

    companion object {
        // Интервал попыток синхронизации (10 минут)
        private const val SYNC_INTERVAL_MS = 10 * 60 * 1000L
    }

    /**
     * Запускает периодическую синхронизацию неотправленных координат
     */
    override fun startSync() {
        if (syncJob?.isActive == true) {
            logger.info(LogCategory.GENERAL, "LocationSyncManager: Sync already running")
            return
        }

        logger.info(LogCategory.GENERAL, "LocationSyncManager: Starting periodic sync")

        syncJob = coroutineScope.launch {
            val connectedLoad = loadRepository.getConnectedLoad() ?: throw IllegalStateException("Connected load not found")
            val serverLoadId = connectedLoad.serverId
            while (isActive) {
                try {
                    // Пытаемся отправить неотправленные координаты
                    val result = uploadPendingLocations(serverLoadId)

                    if (result.isSuccess) {
                        val count = result.getOrNull() ?: 0
                        if (count > 0) {
                            logger.info(LogCategory.GENERAL, "LocationSyncManager: Successfully uploaded $count locations")
                        }
                    } else {
                        logger.info(
                            LogCategory.GENERAL,
                            "LocationSyncManager: Failed to upload locations: ${result.exceptionOrNull()?.message}"
                        )
                    }
                } catch (e: Exception) {
                    logger.info(LogCategory.GENERAL, "LocationSyncManager: Error during sync: ${e.message}")
                }

                // Ждем перед следующей попыткой
                delay(SYNC_INTERVAL_MS)
            }
        }
    }

    /**
     * Останавливает периодическую синхронизацию
     */
    override fun stopSync() {
        logger.info(LogCategory.GENERAL, "LocationSyncManager: Stopping sync")
        syncJob?.cancel()
        syncJob = null
    }

    /**
     * Проверяет, запущена ли синхронизация
     */
    override fun isSyncActive(): Boolean {
        return syncJob?.isActive == true
    }

    /**
     * Отправляет все неотправленные координаты на сервер
     * Использует пакетную отправку для эффективности
     * Обрабатывает большие списки пакетами для избежания проблем с памятью и сетью
     */
    private suspend fun uploadPendingLocations(serverLoadId: Long): Result<Int> {
        return try {
            val unsentLocations = locationRepository.getUnsentDeviceLocations()

            if (unsentLocations.isEmpty()) {
                logger.info(LogCategory.GENERAL, "LocationSyncManager: No pending locations to upload")
                return Result.success(0)
            }

            logger.info(LogCategory.GENERAL, "LocationSyncManager: Found ${unsentLocations.size} pending locations")

            // Максимальный размер пакета для отправки (1000 координат)
            val maxBatchSize = 100
            var totalUploaded = 0

            // Получаем токен для аутентификации
            val authSession = authPrefsRepository.getSession()
            if (authSession == null) {
                logger.info(LogCategory.GENERAL, "LocationSyncManager: No auth session found, cannot upload locations")
                return Result.failure(Exception("No auth session found"))
            }

            if (unsentLocations.size <= maxBatchSize) {
                // Небольшой список - отправляем целиком
                val locations = unsentLocations.map { it.second }
                val result = locationRepository.sendLocations(authSession.token, serverLoadId, locations)

                if (result.isSuccess) {
                    val allIds = unsentLocations.map { it.first }
                    locationRepository.deleteLocationsFromDb(allIds)
                    logger.info(
                        LogCategory.GENERAL,
                        "LocationSyncManager: Successfully uploaded ${unsentLocations.size} locations and deleted from DB"
                    )
                    totalUploaded = unsentLocations.size
                } else {
                    logger.info(
                        LogCategory.GENERAL,
                        "LocationSyncManager: Failed to upload locations: ${result.exceptionOrNull()?.message}"
                    )
                    return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } else {
                // Большой список - обрабатываем пакетами
                logger.info(
                    LogCategory.GENERAL,
                    "LocationSyncManager: Large dataset detected (${unsentLocations.size} locations), " +
                        "processing in batches of $maxBatchSize",
                )

                val batches = unsentLocations.chunked(maxBatchSize)
                val allSuccessfulIds = mutableListOf<Long>()

                batches.forEachIndexed { index, batch ->
                    logger.info(
                        LogCategory.GENERAL,
                        "LocationSyncManager: Processing batch ${index + 1}/${batches.size} (${batch.size} locations)"
                    )

                    val locations = batch.map { it.second }
                    val result = locationRepository.sendLocations(authSession.token, serverLoadId, locations)

                    if (result.isSuccess) {
                        val batchIds = batch.map { it.first }
                        allSuccessfulIds.addAll(batchIds)
                        totalUploaded += batch.size
                        logger.info(
                            LogCategory.GENERAL,
                            "LocationSyncManager: Batch ${index + 1} uploaded successfully (${batch.size} locations)"
                        )
                    } else {
                        logger.info(
                            LogCategory.GENERAL,
                            "LocationSyncManager: Batch ${index + 1} failed: ${result.exceptionOrNull()?.message}"
                        )
                        // Продолжаем с остальными пакетами даже если один не удался
                    }
                }

                // Удаляем все успешно отправленные координаты
                if (allSuccessfulIds.isNotEmpty()) {
                    locationRepository.deleteLocationsFromDb(allSuccessfulIds)
                    logger.info(LogCategory.GENERAL, "LocationSyncManager: Deleted $totalUploaded locations from DB")
                }
            }

            Result.success(totalUploaded)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "LocationSyncManager: Error uploading pending locations: ${e.message}")
            Result.failure(e)
        }
    }
}
