package com.shiplocate.trackingsdk.geofence

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import platform.CoreLocation.CLCircularRegion
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.objc.objc_getClass

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
    private val stopTypeMap = mutableMapOf<Long, Int>() // Map to store stopId -> stopType

    private val delegate = GeofenceLocationDelegate(
        logger = logger,
        onGeofenceEvent = { stopId, isEntered ->
            val stopType = getStopType(stopId)
            scope.launch {
                val event = if (isEntered) {
                    GeofenceEvent.Entered(stopId = stopId, stopType = stopType)
                } else {
                    GeofenceEvent.Exited(stopId = stopId, stopType = stopType)
                }
                geofenceEvents.emit(event)
            }
        },
    )

    companion object {
        private const val TAG = "GeofenceClient"
        // Apple ограничивает количество одновременно отслеживаемых геозон до 20
        private const val MAX_MONITORED_REGIONS = 20
        // Apple рекомендует минимальный радиус 100 метров для надежности
        private const val MIN_RADIUS_METERS = 100.0
    }

    init {
        // Настраиваем locationManager и устанавливаем delegate
        locationManager.delegate = delegate
    }

    /**
     * Добавляет геозону для отслеживания
     * Согласно документации Apple:
     * - Требуется разрешение "Always" для фонового мониторинга
     * - Максимум 20 геозон одновременно
     * - Минимальный радиус рекомендуется 100 метров
     */
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun addGeofence(id: Long, latitude: Double, longitude: Double, radius: Int) {
        try {
            // Проверяем доступность мониторинга регионов
            // В Kotlin/Native для iOS нужно использовать objc_getClass для получения ObjCClass
            val circularRegionClass = objc_getClass("CLCircularRegion") as? ObjCClass
            if (circularRegionClass == null || !CLLocationManager.isMonitoringAvailableForClass(circularRegionClass)) {
                logger.error(
                    LogCategory.LOCATION,
                    "$TAG: Region monitoring is not available on this device",
                )
                return
            }

            // Проверяем статус авторизации
            val authStatus = locationManager.authorizationStatus
            if (authStatus != kCLAuthorizationStatusAuthorizedAlways &&
                authStatus != kCLAuthorizationStatusAuthorizedWhenInUse
            ) {
                logger.warn(
                    LogCategory.LOCATION,
                    "$TAG: Location authorization required for geofencing. Current status: $authStatus",
                )
                // Для фонового мониторинга требуется "Always", но продолжаем для "WhenInUse" тоже
            }

            // Проверяем ограничение на количество геозон (максимум 20)
            if (monitoredRegions.size >= MAX_MONITORED_REGIONS) {
                logger.error(
                    LogCategory.LOCATION,
                    "$TAG: Maximum number of monitored regions ($MAX_MONITORED_REGIONS) reached. Cannot add geofence for stop $id",
                )
                return
            }

            // Проверяем, не отслеживается ли уже эта геозона
            if (monitoredRegions.containsKey(id)) {
                logger.warn(
                    LogCategory.LOCATION,
                    "$TAG: Geofence for stop $id is already being monitored",
                )
                return
            }

            // Проверяем минимальный радиус (рекомендация Apple - минимум 100 метров)
            val radiusDouble = radius.toDouble()
            if (radiusDouble < MIN_RADIUS_METERS) {
                logger.warn(
                    LogCategory.LOCATION,
                    "$TAG: Radius $radius meters is less than recommended minimum $MIN_RADIUS_METERS meters for stop $id",
                )
            }

            // Создаем регион
            val region = CLCircularRegion(
                center = platform.CoreLocation.CLLocationCoordinate2DMake(latitude, longitude),
                radius = radiusDouble.coerceAtLeast(MIN_RADIUS_METERS), // Используем минимум 100м
                identifier = id.toString(),
            ).apply {
                notifyOnEntry = true
                notifyOnExit = false
            }

            // Начинаем мониторинг
            locationManager.startMonitoringForRegion(region)
            monitoredRegions[id] = region
            // Store stopType (default to 0, should be passed as parameter in future)
            stopTypeMap[id] = 0

            logger.info(
                LogCategory.LOCATION,
                "$TAG: Geofence added for stop $id (lat=$latitude, lng=$longitude, radius=${region.radius})",
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
                stopTypeMap.remove(id)
                logger.info(LogCategory.LOCATION, "$TAG: Geofence removed for stop $id")
            } else {
                logger.warn(LogCategory.LOCATION, "$TAG: Geofence not found for stop $id")
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
            stopTypeMap.clear()

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
     * Получает stopType для stopId
     */
    private fun getStopType(stopId: Long): Int {
        return stopTypeMap[stopId] ?: 0
    }
}

/**
 * Delegate для обработки событий геозон в CLLocationManager
 */
@OptIn(ExperimentalForeignApi::class)
private class GeofenceLocationDelegate(
    private val logger: Logger,
    private val onGeofenceEvent: (stopId: Long, isEntered: Boolean) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {

    @ObjCSignatureOverride
    override fun locationManager(
        manager: CLLocationManager,
        didEnterRegion: platform.CoreLocation.CLRegion,
    ) {
        val stopId = didEnterRegion.identifier.toLongOrNull()
        if (stopId != null) {
            onGeofenceEvent(stopId, true)
        }
    }

    @ObjCSignatureOverride
    override fun locationManager(
        manager: CLLocationManager,
        didExitRegion: platform.CoreLocation.CLRegion,
    ) {
        val stopId = didExitRegion.identifier.toLongOrNull()
        if (stopId != null) {
            onGeofenceEvent(stopId, false)
        }
    }

    @ObjCSignatureOverride
    override fun locationManager(
        manager: CLLocationManager,
        monitoringDidFailForRegion: platform.CoreLocation.CLRegion?,
        withError: NSError,
    ) {
        // Handle monitoring errors according to Apple documentation
        val regionId = monitoringDidFailForRegion?.identifier ?: "unknown"
        logger.error(
            LogCategory.LOCATION,
            "GeofenceLocationDelegate: Monitoring failed for region $regionId. Error: ${withError.localizedDescription} (code: ${withError.code})",
        )
    }
}

