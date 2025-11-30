package com.shiplocate.di

import android.content.Context
import com.shiplocate.ActivityProvider
import com.shiplocate.AndroidPermissionChecker
import com.shiplocate.AndroidPermissionManagerImpl
import com.shiplocate.data.datasource.PermissionManager
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

    // AndroidPermissionChecker для проверки разрешений (требует Context)
    single<AndroidPermissionChecker> {
        AndroidPermissionChecker(get<Context>())
    }

    // PermissionManager для domain слоя
    // Создаем экземпляр AndroidPermissionManagerImpl
    single<AndroidPermissionManagerImpl> {
        AndroidPermissionManagerImpl(get(), get(), get())
    }

    // Регистрируем тот же экземпляр как PermissionManager
    single<PermissionManager> {
        get<AndroidPermissionManagerImpl>()
    }
}
