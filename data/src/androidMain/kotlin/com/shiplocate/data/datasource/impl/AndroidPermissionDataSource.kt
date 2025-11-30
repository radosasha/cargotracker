package com.shiplocate.data.datasource.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.PermissionManager
import com.shiplocate.data.datasource.impl.AndroidGpsManager.Companion.INTERVAL_MS
import com.shiplocate.data.datasource.impl.AndroidGpsManager.Companion.MIN_DISTANCE_M
import com.shiplocate.data.datasource.impl.AndroidGpsManager.Companion.MIN_UPDATE_MS
import com.shiplocate.data.model.PermissionDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine

/**
 * Android реализация PermissionDataSource
 */
class AndroidPermissionDataSource(
    private val permissionManager: PermissionManager,
    private val logger: Logger,
    private val context: Context,
) : PermissionDataSource {
    // Flow для уведомлений о получении разрешений
    private val permissionsFlow = MutableSharedFlow<PermissionDataModel>(replay = 0)

    // Coroutine scope для эмита в Flow
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        context.registerReceiver(AirplaneReceiver(), filter)
    }

    override suspend fun getPermissionStatus(): PermissionDataModel {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            INTERVAL_MS,
        )
            .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
            .setMaxUpdateDelayMillis(INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_MS)
            .build()

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val locationSatisfied = suspendCoroutine { cont ->
            val client = LocationServices.getSettingsClient(context)
            client.checkLocationSettings(settingsRequest)
                .addOnSuccessListener {
                    cont.resumeWith(Result.success(true))
                }
                .addOnFailureListener { e ->
                    when (e) {
                        is ResolvableApiException -> {
                            logger.error(
                                LogCategory.LOCATION,
                                "AndroidPermissionDataSource: Location settings are not satisfied and need solution: message=${e.message}, error=$e"
                            )
                            val manufacturer = Build.MANUFACTURER
                            if (!manufacturer.isNullOrEmpty() && manufacturer.lowercase().startsWith("samsung") && isHighAccuracyEnabled(context)) {
                                cont.resumeWith(Result.success(true))
                            } else {
                                cont.resumeWith(Result.success(false))
                            }
                        }

                        else -> {
                            logger.error(
                                LogCategory.LOCATION,
                                "AndroidPermissionDataSource: Location settings are not satisfied: message=${e.message}, error=$e"
                            )
                            cont.resumeWith(Result.success(false))
                        }
                    }
                }
        }
        return PermissionDataModel(
            hasLocationPermission = permissionManager.hasLocationPermissions(),
            hasBackgroundLocationPermission = permissionManager.hasBackgroundLocationPermission(),
            hasNotificationPermission = permissionManager.hasNotificationPermission(),
//            hasActivityRecognitionPermission = permissionManager.hasActivityRecognitionPermission(),
            isBatteryOptimizationDisabled = permissionManager.isBatteryOptimizationDisabled(),
            isHighAccuracyEnabled = locationSatisfied,
            inAirplaneMode = isAirplaneOn(),
        )
    }

    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return try {
            permissionManager.requestNotificationPermission()
            Result.success(permissionManager.hasNotificationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestLocationPermission(): Result<PermissionDataModel> {
        return try {
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestLocationPermission() called")
            permissionManager.requestLocationPermission()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestLocationPermission() - status: location=${status.hasLocationPermission}",
            )

            Result.success(status)
        } catch (e: Exception) {
            logger.error(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestLocationPermission() - exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<PermissionDataModel> {
        return try {
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestBackgroundLocationPermission() called")
            permissionManager.requestBackgroundLocationPermission()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestBackgroundLocationPermission() - status: background=${status.hasBackgroundLocationPermission}",
            )

            Result.success(status)
        } catch (e: Exception) {
            logger.error(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestBackgroundLocationPermission() - exception: ${e.message}",
                e
            )
            Result.failure(e)
        }
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<PermissionDataModel> {
        return try {
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestBatteryOptimizationDisable() called")
            permissionManager.requestBatteryOptimizationDisable()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestBatteryOptimizationDisable() - status: battery=${status.isBatteryOptimizationDisabled}",
            )

            Result.success(status)
        } catch (e: Exception) {
            logger.error(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestBatteryOptimizationDisable() - exception: ${e.message}",
                e
            )
            Result.failure(e)
        }
    }

    override suspend fun requestEnableHighAccuracy(): Result<PermissionDataModel> {
        return try {
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestEnableGps() called")
            permissionManager.requestEnableHighAccuracy()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestEnableGps() - status: locationEnabled=${status.isHighAccuracyEnabled}",
            )

            Result.success(status)
        } catch (e: Exception) {
            logger.error(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestEnableGps() - exception: ${e.message}",
                e
            )
            Result.failure(e)
        }
    }

    override suspend fun openAirplaneModeSettings(): Result<Unit> {
        return try {
            permissionManager.openAirplaneModeSettings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun notifyPermissionGranted() {
        logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.notifyPermissionGranted() called")
        scope.launch {
            val status = getPermissionStatus()
            permissionsFlow.emit(status)
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource: Emitted permission status to flow")
        }
    }

    override fun observePermissions(): Flow<PermissionDataModel> {
        logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.observePermissions() called")
        return permissionsFlow
    }

    private fun isAirplaneOn(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) == 1
    }

    private inner class AirplaneReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                scope.launch {
                    permissionsFlow.emit(getPermissionStatus())
                }
            }
        }
    }

    fun isHighAccuracyEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            ) == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY
        } catch (e: Exception) {
            false
        }
    }
}
