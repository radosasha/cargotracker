package com.shiplocate.di

import com.shiplocate.data.datasource.AndroidFirebaseTokenServiceDataSourceImpl
import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import org.koin.dsl.module

/**
 * Android платформо-специфичный модуль для composeApp
 */
val androidPlatformModule =
    module {

        // Переопределяем FirebaseTokenServiceDataSource для Android
        single<FirebaseTokenServiceDataSource> { AndroidFirebaseTokenServiceDataSourceImpl() }
    }
