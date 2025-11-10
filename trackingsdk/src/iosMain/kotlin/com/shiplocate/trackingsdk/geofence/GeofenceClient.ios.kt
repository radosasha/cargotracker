package com.shiplocate.trackingsdk.geofence

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Stop
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
        private const val GEOFENCE_RADIUS_DEFAULT = 100.0 // метров
    }

    init {
        // Настраиваем locationManager
        // TODO: Установить delegate для обработки событий геозон
    }

    /**
     * Добавляет геозону для отслеживания
     */
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun addGeofence(stop: Stop) {
        try {
            // TODO: получить координаты из stop.locationAddress или использовать locationId
            // Пока используем дефолтные координаты, нужно будет добавить координаты в Stop
            val radius = (stop.geofenceRadius ?: GEOFENCE_RADIUS_DEFAULT.toInt()).toDouble()

            val region = CLCircularRegion(
                center = platform.CoreLocation.CLLocationCoordinate2DMake(stop.latitude, stop.longitude),
                radius = radius,
                identifier = stop.id.toString(),
            ).apply {
                notifyOnEntry = true
                notifyOnExit = true
            }

            locationManager.startMonitoringForRegion(region)
            monitoredRegions[stop.id] = region

            logger.info(
                LogCategory.LOCATION,
                "$TAG: Geofence added for stop ${stop.id} (type: ${stop.type})",
            )
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error adding geofence for stop ${stop.id}: ${e.message}",
                e,
            )
        }
    }

    /**
     * Удаляет геозону
     */
    actual suspend fun removeGeofence(stopId: Long) {
        try {
            val region = monitoredRegions.remove(stopId)
            if (region != null) {
                locationManager.stopMonitoringForRegion(region)
                logger.info(LogCategory.LOCATION, "$TAG: Geofence removed for stop $stopId")
            }
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error removing geofence for stop $stopId: ${e.message}",
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

    /**
     * Очищает ресурсы
     */
    actual suspend fun destroy() {
        logger.info(LogCategory.LOCATION, "$TAG: Destroying GeofenceClient")
        removeAllGeofences()
    }
}

