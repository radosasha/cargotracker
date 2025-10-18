package com.tracker.di

import com.tracker.ActivityProvider
import com.tracker.AndroidPermissionRequesterImpl
import com.tracker.AndroidTrackingRequesterImpl
import com.tracker.PermissionChecker
import com.tracker.data.datasource.PermissionRequester
import com.tracker.data.datasource.TrackingRequester
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
