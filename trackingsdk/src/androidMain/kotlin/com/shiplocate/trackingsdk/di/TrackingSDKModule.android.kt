package com.shiplocate.trackingsdk.di

import android.content.Context
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.TrackingSDK
import com.shiplocate.trackingsdk.TrackingSDKAndroid
import com.shiplocate.trackingsdk.TrackingSDKFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-специфичный DI модуль для TrackingSDK
 */
actual val trackingSDKModule: Module = module {
    
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

    single<TrackingManager> { TrackingManager(get(), get()) }
}
