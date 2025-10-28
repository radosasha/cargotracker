package com.shiplocate.trackingsdk.di

import com.shiplocate.trackingsdk.IOSTrackingService
import com.shiplocate.trackingsdk.TrackingSDK
import com.shiplocate.trackingsdk.TrackingSDKFactory
import com.shiplocate.trackingsdk.TrackingSDKIOS
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-специфичный DI модуль для TrackingSDK
 */
actual val trackingSDKModule: Module = module {

    // Регистрируем TrackingSDK как singleton
    single<TrackingSDK> {
        val sdk = TrackingSDKIOS(get())

        // Устанавливаем экземпляр в фабрику
        TrackingSDKFactory.setInstance(sdk)

        TrackingSDKFactory.getInstance()
    }

    single<IOSTrackingService> {
        IOSTrackingService(get(), get(), get())
    }
}
