package com.shiplocate.trackingsdk.di

import android.content.Context
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.TrackingManager
import com.shiplocate.trackingsdk.TrackingSDK
import com.shiplocate.trackingsdk.TrackingSDKAndroid
import com.shiplocate.trackingsdk.TrackingSDKFactory
import com.shiplocate.trackingsdk.TripRecorder
import com.shiplocate.trackingsdk.motion.ActivityRecognitionConnector
import com.shiplocate.trackingsdk.motion.MotionTracker
import com.shiplocate.trackingsdk.parking.ParkingTracker
import com.shiplocate.trackingsdk.utils.ParkingTimeoutTimer
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-специфичный DI модуль для TrackingSDK
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
        ParkingTimeoutTimer(get())
    }

    // Регистрируем ParkingTracker
    single<ParkingTracker> {
        ParkingTracker(get(), 200, 20, get())
    }

    // Регистрируем ActivityRecognitionConnector
    single<ActivityRecognitionConnector> {
        ActivityRecognitionConnector(get(), get(), get())
    }

    // Регистрируем MotionTracker
    single<MotionTracker> {
        MotionTracker(get(), get())
    }

    // Регистрируем TrackingManager
    single<TrackingManager> {
        TrackingManager(get(), get(), get(), get(), get())
    }

    // Регистрируем TrackingSDK как singleton
    single<TrackingSDK> {
        val sdk = TrackingSDKAndroid(
            context = get<Context>(),
            logger = get<Logger>()
        )

        // Устанавливаем экземпляр в фабрику
        TrackingSDKFactory.setInstance(sdk)
        TrackingSDKFactory.getInstance()
    }

    single<ActivityRecognitionClient> {
        ActivityRecognition.getClient(get())
    }
}
