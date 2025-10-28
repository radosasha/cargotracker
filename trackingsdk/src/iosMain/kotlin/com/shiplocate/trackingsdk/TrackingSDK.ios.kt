package com.shiplocate.trackingsdk

import org.koin.core.component.KoinComponent

/**
 * iOS реализация TrackingSDK
 * Объединяет функциональность IOSLocationTrackingService
 */
class TrackingSDKIOS(val trackingService: IOSTrackingService) : TrackingSDK, KoinComponent {

    private var isTracking = false

    companion object {
        private const val TAG = "TrackingSDKAndroid"
    }

    override suspend fun startTracking(): Result<Unit> {
        isTracking = true
        return trackingService.startTracking()
    }

    override suspend fun stopTracking(): Result<Unit> {
        isTracking = false
        return trackingService.stopTracking()
    }

    override suspend fun isTrackingActive(): Boolean {
        return isTracking
    }

    override fun getServiceStatus(): String {
        return "TrackingSDKAndroid(status=${if (isTracking) "TRACKING" else "STOPPED"})"
    }

    override fun destroy() {
        trackingService.destroy()
        isTracking = false
    }
}
