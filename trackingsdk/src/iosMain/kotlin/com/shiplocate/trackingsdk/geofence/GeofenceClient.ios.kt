package com.shiplocate.trackingsdk.geofence

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.CoreLocation.CLCircularRegion
import platform.CoreLocation.CLLocationManager

/**
 * iOS actual реализация GeofenceClient
 * Использует CoreLocation CLLocationManager для отслеживания геозон
 */
actual class GeofenceClient(
    private val logger: Logger,
    private val scope: CoroutineScope,
) {
    private val locationManager = CLLocationManager()
    private val geofenceEvents = MutableSharedFlow<GeofenceEvent>(replay = 0)
    private val monitoredRegions = mutableMapOf<Long, CLCircularRegion>()

    companion object {
        private const val TAG = "GeofenceClient"
    }

    init {
        // Настраиваем locationManager
        // TODO: Установить delegate для обработки событий геозон
    }

    /**
     * Добавляет геозону для отслеживания
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun addGeofence(id: Long, latitude: Double, longitude: Double, radius: Int) {
        try {

            val region = CLCircularRegion(
                center = platform.CoreLocation.CLLocationCoordinate2DMake(latitude, longitude),
                radius = radius.toDouble(),
                identifier = id.toString(),
            ).apply {
                notifyOnEntry = true
                notifyOnExit = false
            }

            locationManager.startMonitoringForRegion(region)
            monitoredRegions[id] = region

            logger.info(
                LogCategory.LOCATION,
                "$TAG: Geofence added for stop $id",
            )
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error adding geofence for stop $id: ${e.message}",
                e,
            )
        }
    }

    /**
     * Удаляет геозону
     */
    actual suspend fun removeGeofence(id: Long) {
        try {
            val region = monitoredRegions.remove(id)
            if (region != null) {
                locationManager.stopMonitoringForRegion(region)
                logger.info(LogCategory.LOCATION, "$TAG: Geofence removed for stop $id")
            }
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error removing geofence for stop $id: ${e.message}",
                e,
            )
        }
    }

    /**
     * Удаляет все геозоны
     */
    actual suspend fun removeAllGeofences() {
        try {
            monitoredRegions.values.forEach { region ->
                locationManager.stopMonitoringForRegion(region)
            }
            monitoredRegions.clear()

            logger.info(LogCategory.LOCATION, "$TAG: All geofences removed")
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error removing all geofences: ${e.message}",
                e,
            )
        }
    }

    /**
     * Поток событий геозон
     */
    actual fun observeGeofenceEvents(): Flow<GeofenceEvent> {
        return geofenceEvents
    }
}

