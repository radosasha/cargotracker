package com.shiplocate.di

import com.shiplocate.IOSPermissionCheckerImpl
import com.shiplocate.IOSPermissionRequesterImpl
import com.shiplocate.IOSTrackingRequesterImpl
import com.shiplocate.data.datasource.PermissionChecker
import com.shiplocate.data.datasource.PermissionRequester
import com.shiplocate.data.datasource.TrackingRequester
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для composeApp (ViewController scope)
 */
val iosModule =
    module {

        // iOS Permission Checker (Singleton - живет весь жизненный цикл приложения)
        single<PermissionChecker> { IOSPermissionCheckerImpl(get()) }

        // iOS Permission Requester для domain слоя
        single<PermissionRequester> { IOSPermissionRequesterImpl() }

        // iOS Tracking Requester (Singleton - живет весь жизненный цикл приложения)
        single<TrackingRequester> { IOSTrackingRequesterImpl() }
    }
