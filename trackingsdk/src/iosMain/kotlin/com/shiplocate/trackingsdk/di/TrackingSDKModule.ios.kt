package com.shiplocate.trackingsdk.di

import com.shiplocate.trackingsdk.IOSTrackingService
import com.shiplocate.trackingsdk.TrackingSDK
import com.shiplocate.trackingsdk.TrackingSDKFactory
import com.shiplocate.trackingsdk.TrackingSDKIOS
import com.shiplocate.trackingsdk.TripRecorder
import com.shiplocate.trackingsdk.parking.ParkingTracker
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

    // Регистрируем ParkingTracker
    single<ParkingTracker> {
        ParkingTracker(get(), 200, 20, get())
    }

    // Регистрируем TrackingManager
    single<TrackingManager> {
        TrackingManager(get(), get(), get(), get())
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
