package com.shiplocate.di

import com.shiplocate.ActivityProvider
import com.shiplocate.AndroidPermissionRequesterImpl
import com.shiplocate.data.datasource.PermissionRequester
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

    // PermissionRequester для domain слоя
    single<PermissionRequester> {
        AndroidPermissionRequesterImpl()
    }
}
