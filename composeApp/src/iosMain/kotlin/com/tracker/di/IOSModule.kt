package com.tracker.di

import com.tracker.IOSPermissionCheckerImpl
import com.tracker.data.datasource.impl.IOSLocationServiceImpl
import com.tracker.domain.datasource.IOSLocationService
import com.tracker.data.datasource.PermissionChecker
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для composeApp (ViewController scope)
 */
val iosModule = module {
    
    // iOS Location Manager (Singleton - живет весь жизненный цикл приложения)
    single<IOSLocationService> { IOSLocationServiceImpl() }
    
    // iOS Permission Checker (Singleton - живет весь жизненный цикл приложения)
    single<PermissionChecker> { IOSPermissionCheckerImpl() }
    
}
