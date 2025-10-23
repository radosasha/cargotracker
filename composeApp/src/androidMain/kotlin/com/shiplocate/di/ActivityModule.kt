package com.shiplocate.di

import com.shiplocate.ActivityProvider
import com.shiplocate.AndroidPermissionRequesterImpl
import com.shiplocate.AndroidTrackingRequesterImpl
import com.shiplocate.PermissionChecker
import com.shiplocate.data.datasource.PermissionRequester
import com.shiplocate.data.datasource.TrackingRequester
import org.koin.dsl.module

/**
 * Koin модуль для Activity Context
 * Живет только в жизненном цикле Activity
 */
val activityModule = module {
    // ActivityContextProvider как singleton в scope Activity
    single<ActivityProvider> {
        ActivityProvider()
    }

    // PermissionChecker как singleton в scope Activity
    single<PermissionChecker> {
        PermissionChecker()
    }

    // PermissionRequester для domain слоя
    single<PermissionRequester> {
        AndroidPermissionRequesterImpl()
    }

    // TrackingRequester для domain слоя
    single<TrackingRequester> {
        AndroidTrackingRequesterImpl()
    }
}
