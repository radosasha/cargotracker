package com.shiplocate.di

import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import com.shiplocate.data.datasource.IOSFirebaseTokenServiceDataSourceImpl
import org.koin.dsl.module

/**
 * iOS платформо-специфичный модуль для composeApp
 */
val iosPlatformModule = module {

        // Переопределяем FirebaseTokenServiceDataSource для iOS
        single<FirebaseTokenServiceDataSource> { IOSFirebaseTokenServiceDataSourceImpl() }
    }
