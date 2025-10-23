package com.shiplocate

import android.content.Intent
import com.shiplocate.data.datasource.TrackingRequester
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация TrackingRequester
 */
class AndroidTrackingRequesterImpl : TrackingRequester, KoinComponent {
    private val activityContextProvider: ActivityProvider by inject()

    private var isTracking = false

    override suspend fun startTracking(): Result<Unit> {
        return try {
            val context = activityContextProvider.getActivity()
            val intent = Intent(context, AndroidTrackingService::class.java)
            context.startForegroundService(intent)
            isTracking = true
            println("AndroidTrackingRequesterImpl: Tracking started, isTracking = $isTracking")
            Result.success(Unit)
        } catch (e: Exception) {
            println("AndroidTrackingRequesterImpl: Error starting tracking: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun stopTracking(): Result<Unit> {
        return try {
            val context = activityContextProvider.getActivity()
            val intent = Intent(context, AndroidTrackingService::class.java)
            context.stopService(intent)
            isTracking = false
            println("AndroidTrackingRequesterImpl: Tracking stopped, isTracking = $isTracking")
            Result.success(Unit)
        } catch (e: Exception) {
            println("AndroidTrackingRequesterImpl: Error stopping tracking: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun isTrackingActive(): Boolean {
        println("AndroidTrackingRequesterImpl: isTrackingActive() = $isTracking")
        return isTracking
    }
}
