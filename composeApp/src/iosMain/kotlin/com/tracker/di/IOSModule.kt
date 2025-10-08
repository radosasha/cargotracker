package com.tracker.di

import com.tracker.IOSPermissionCheckerImpl
import com.tracker.IOSTrackingRequesterImpl
import com.tracker.data.datasource.TrackingRequester
import com.tracker.data.datasource.PermissionChecker
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для composeApp (ViewController scope)
 */
val iosModule = module {
    
    // iOS Permission Checker (Singleton - живет весь жизненный цикл приложения)
    single<PermissionChecker> { IOSPermissionCheckerImpl() }
    
    // iOS Tracking Requester (Singleton - живет весь жизненный цикл приложения)
    single<TrackingRequester> { IOSTrackingRequesterImpl() }
    
}
