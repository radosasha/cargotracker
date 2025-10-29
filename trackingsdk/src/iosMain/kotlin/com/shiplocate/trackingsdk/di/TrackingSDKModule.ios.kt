package com.shiplocate.trackingsdk.di

import com.shiplocate.trackingsdk.IOSTrackingService
import com.shiplocate.trackingsdk.TrackingManager
import com.shiplocate.trackingsdk.TrackingSDK
import com.shiplocate.trackingsdk.TrackingSDKFactory
import com.shiplocate.trackingsdk.TrackingSDKIOS
import com.shiplocate.trackingsdk.TripRecorder
import com.shiplocate.trackingsdk.motion.ActivityRecognitionConnector
import com.shiplocate.trackingsdk.motion.MotionTracker
import com.shiplocate.trackingsdk.parking.ParkingTracker
import com.shiplocate.trackingsdk.utils.ParkingTimeoutTimer
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-специфичный DI модуль для TrackingSDK
 */
actual val trackingSDKModule: Module = module {

    // Регистрируем TripRecorder
    single<TripRecorder> {
        TripRecorder(
            locationRepository = get(),
            gpsRepository = get(),
            locationProcessor = get(),
            deviceRepository = get(),
            loadRepository = get(),
            logger = get()
        )
    }

    // Регистрируем ParkingTimeoutTimer
    single<ParkingTimeoutTimer> {
        ParkingTimeoutTimer(
            timeoutMs = 20 * 60 * 1000,
            scope = get()
        )
    }

    // Регистрируем ParkingTracker
    single<ParkingTracker> {
        ParkingTracker(
            parkingTimeoutTimer = get(),
            parkingRadiusMeters = 200,
            triggerTimeMs = 20 * 60 * 1000L,
            logger = get()
        )
    }

    // Регистрируем ActivityRecognitionConnector
    single<ActivityRecognitionConnector> {
        ActivityRecognitionConnector(
            activityFrequencyMs = 10000L, // 10 секунд, как на Android
            logger = get(),
            scope = get()
        )
    }

    // Регистрируем MotionTracker
    single<MotionTracker> {
        MotionTracker(
            activityRecognitionConnector = get(),
            analysisWindowMs = 3 * 60 * 1000L,
            trimWindowMs = 1 * 60 * 1000L,
            vehicleTimeThreshold = 0.6f,
            confidenceThreshold = 70,
            minAnalysisDurationMs = 1 * 60 * 1000,
            logger = get(),
            scope = get()
        )
    }

    // Регистрируем TrackingManager
    single<TrackingManager> {
        TrackingManager(
            tripRecorder = get(),
            locationSyncService = get(),
            parkingTracker = get(),
            motionTracker = get(),
            logger = get(),
            scope = get()
        )
    }

    // Регистрируем IOSTrackingService
    single<IOSTrackingService> {
        IOSTrackingService(get(), get())
    }

    // Регистрируем TrackingSDK как singleton
    single<TrackingSDK> {
        val sdk = TrackingSDKIOS(get())

        // Устанавливаем экземпляр в фабрику
        TrackingSDKFactory.setInstance(sdk)

        TrackingSDKFactory.getInstance()
    }
}
