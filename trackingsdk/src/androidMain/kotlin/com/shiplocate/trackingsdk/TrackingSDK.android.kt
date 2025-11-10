package com.shiplocate.trackingsdk

import android.content.Context
import android.content.Intent
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger

/**
 * Android реализация TrackingSDK
 */
class TrackingSDKAndroid(
    private val context: Context,
    private val logger: Logger
) : TrackingSDK {

    private var isTracking = false

    companion object {
        private const val TAG = "TrackingSDKAndroid"
    }

    override suspend fun startTracking(loadId: Long): Result<Unit> {
        return try {

            // Используем AndroidTrackingService из trackingsdk модуля
            val intent = Intent(context, AndroidTrackingService::class.java)
            intent.putExtra(AndroidTrackingService.EXTRA_LOAD_ID, loadId)
            context.startForegroundService(intent)
            isTracking = true

            logger.info(LogCategory.LOCATION, "$TAG: Tracking started with loadId=$loadId, isTracking = $isTracking")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: Error starting tracking: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun stopTracking(): Result<Unit> {
        return try {

            val intent = Intent(context, AndroidTrackingService::class.java)
            context.stopService(intent)
            isTracking = false

            logger.info(LogCategory.LOCATION, "$TAG: Tracking stopped, isTracking = $isTracking")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "$TAG: Error stopping tracking: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun isTrackingActive(): Boolean {
        logger.debug(LogCategory.LOCATION, "$TAG: isTrackingActive() = $isTracking")
        return isTracking
    }

    override fun getServiceStatus(): String {
        return "TrackingSDKAndroid(status=${if (isTracking) "TRACKING" else "STOPPED"})"
    }

    override fun destroy() {
        logger.info(LogCategory.LOCATION, "$TAG: Destroying SDK")
        isTracking = false
    }
}
