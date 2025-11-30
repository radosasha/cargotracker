package com.shiplocate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import androidx.core.net.toUri

/**
 * Android класс для запроса разрешений
 * Использует только Android API для определения состояния разрешений,
 * без использования SharedPreferences
 */
class AndroidPermissionRequester(
    private val activityProvider: ActivityProvider,
    private val logger: Logger,
) {

    private fun logInfo(message: String) = logger.info(LogCategory.PERMISSIONS, message)
    private fun logError(message: String, throwable: Throwable? = null) =
        logger.error(LogCategory.PERMISSIONS, message, throwable)

    fun shouldShowLocationPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activityProvider.getActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun shouldShowActivityRecognitionPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activityProvider.getActivity(),
            Manifest.permission.ACTIVITY_RECOGNITION,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun shouldShowBackgroundLocationPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activityProvider.getActivity(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
    }

    fun shouldShowNotificationPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activityProvider.getActivity(),
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            false
        }
    }

    fun requestLocationPermissions() {
        logInfo("Requesting location permissions")
        ActivityCompat.requestPermissions(
            activityProvider.getActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundLocationPermission() {
        logInfo("Requesting background location permission")
        ActivityCompat.requestPermissions(
            activityProvider.getActivity(),
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            MainActivity.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            activityProvider.getActivity(),
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            MainActivity.REQUEST_NOTIFICATIONS_PERMISSION,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestActivityRecognitionPermission() {
        logInfo("Requesting activity recognition permission")
        ActivityCompat.requestPermissions(
            activityProvider.getActivity(),
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            MainActivity.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE,
        )
    }

    fun requestBatteryOptimizationDisable() {
        logInfo("Requesting battery optimization disable")
        try {
            val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${activityProvider.getActivity().packageName}".toUri()
                }
            activityProvider.getActivity().startActivity(intent)
            logInfo("Battery optimization request sent")
        } catch (e: Exception) {
            logError("Error requesting battery optimization disable", e)
            // Fallback to battery optimization settings page
            openBatteryOptimizationSettings()
        }
    }

    fun openLocationSettings() {
        logInfo("Opening location settings")
        try {
            // Открываем конкретную страницу разрешений на местоположение
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activityProvider.getActivity().packageName, null)
                // Добавляем extra для перехода к разрешениям
                putExtra("extra_show_fragment", "com.android.settings.applications.InstalledAppDetails")
                putExtra("extra_show_fragment_args", "permissions")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityProvider.getActivity().startActivity(intent)
            logInfo("Location settings opened successfully")
        } catch (e: Exception) {
            logError("Error opening location settings", e)
            // Fallback: попробуем открыть настройки разрешений напрямую
            try {
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activityProvider.getActivity().packageName, null)
                }
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activityProvider.getActivity().startActivity(fallbackIntent)
                logInfo("Fallback location settings opened successfully")
            } catch (e2: Exception) {
                logError("Error opening fallback location settings", e2)
                // Последний fallback - общие настройки приложения
                openAppSettings()
            }
        }
    }

    fun openNotificationSettings() {
        logInfo("Opening notification settings")
        try {
            val intent =
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, activityProvider.getActivity().packageName)
                }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityProvider.getActivity().startActivity(intent)
            logInfo("Notification settings opened successfully")
        } catch (e: Exception) {
            logError("Error opening notification settings", e)
            // Fallback to general app settings
            openAppSettings()
        }
    }

    fun openBatteryOptimizationSettings() {
        logInfo("Opening battery optimization settings")
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityProvider.getActivity().startActivity(intent)
            logInfo("Battery optimization settings opened successfully")
        } catch (e: Exception) {
            logError("Error opening battery optimization settings", e)
            // Fallback to general app settings
            openAppSettings()
        }
    }

    fun openAirplaneModeSettings() {
        logInfo("Opening airplane mode settings")
        try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityProvider.getActivity().startActivity(intent)
            logInfo("Airplane mode settings opened successfully")
        } catch (e: Exception) {
            logError("Error opening airplane mode settings", e)
            openAppSettings()
        }
    }

    fun openAppSettings() {
        logInfo("Opening app settings")
        try {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activityProvider.getActivity().packageName, null)
                }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityProvider.getActivity().startActivity(intent)
            logInfo("App settings opened successfully")
        } catch (e: Exception) {
            logError("Error opening app settings", e)
        }
    }
}
