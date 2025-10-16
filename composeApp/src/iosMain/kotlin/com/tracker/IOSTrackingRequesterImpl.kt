package com.tracker

import com.tracker.data.datasource.TrackingRequester
import org.koin.core.component.KoinComponent

/**
 * iOS реализация TrackingRequester
 * Использует IOSLocationTrackingService singleton
 */
class IOSTrackingRequesterImpl : TrackingRequester, KoinComponent {
    override suspend fun startTracking(): Result<Unit> {
        return try {
            // Используем singleton IOSLocationTrackingService
            IOSLocationTrackingService.startTracking()
            val serviceActive = IOSLocationTrackingService.isTrackingActive()

            if (serviceActive) {
                println("IOSTrackingRequesterImpl: Tracking started successfully")
                Result.success(Unit)
            } else {
                println("IOSTrackingRequesterImpl: Failed to start tracking - service not active")
                Result.failure(Exception("Failed to start tracking"))
            }
        } catch (e: Exception) {
            println("IOSTrackingRequesterImpl: Error starting tracking: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun stopTracking(): Result<Unit> {
        return try {
            // Используем singleton IOSLocationTrackingService
            IOSLocationTrackingService.stopTracking()
            val serviceActive = IOSLocationTrackingService.isTrackingActive()

            if (!serviceActive) {
                println("IOSTrackingRequesterImpl: Tracking stopped successfully")
                Result.success(Unit)
            } else {
                println("IOSTrackingRequesterImpl: Failed to stop tracking - service still active")
                Result.failure(Exception("Failed to stop tracking"))
            }
        } catch (e: Exception) {
            println("IOSTrackingRequesterImpl: Error stopping tracking: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun isTrackingActive(): Boolean {
        // Проверяем статус только через singleton (убрали дублирование состояния)
        val serviceActive = IOSLocationTrackingService.isTrackingActive()
        println("IOSTrackingRequesterImpl: isTrackingActive() = $serviceActive")
        return serviceActive
    }
}
