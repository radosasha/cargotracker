package com.tracker.di

import com.tracker.IOSLocationManager
import com.tracker.IOSLocationService
import com.tracker.IOSPermissionCheckerImpl
import com.tracker.IOSTrackingRequesterImpl
import com.tracker.domain.datasource.LocationManager
import com.tracker.data.datasource.PermissionChecker
import com.tracker.domain.datasource.TrackingRequester
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для composeApp (ViewController scope)
 */
val iosModule = module {
    
    // iOS Location Manager (Singleton - живет весь жизненный цикл приложения)
    single<LocationManager> { IOSLocationManager() }
    
    // iOS Location Service (Singleton - живет весь жизненный цикл приложения)
    single { IOSLocationService() }
    
    // iOS Tracking Requester (Singleton - живет весь жизненный цикл приложения)
    single<TrackingRequester> { IOSTrackingRequesterImpl() }
    
    // iOS Permission Checker (Singleton - живет весь жизненный цикл приложения)
    single<PermissionChecker> { IOSPermissionCheckerImpl() }
    
}
