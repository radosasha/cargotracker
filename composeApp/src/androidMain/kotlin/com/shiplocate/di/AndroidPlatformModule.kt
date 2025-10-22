package com.shiplocate.di

import com.shiplocate.data.datasource.FirebaseTokenService
import com.shiplocate.data.datasource.FirebaseTokenServiceAdapter
import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import org.koin.dsl.module

/**
 * Android платформо-специфичный модуль для composeApp
 */
val androidPlatformModule = module {

    // Регистрируем actual класс FirebaseTokenServiceDataSource для Android
    single<FirebaseTokenServiceDataSource> { FirebaseTokenServiceDataSource(get()) }
    
    // Регистрируем адаптер для связи с интерфейсом из data модуля
    single<FirebaseTokenService> { FirebaseTokenServiceAdapter(get()) }
}
