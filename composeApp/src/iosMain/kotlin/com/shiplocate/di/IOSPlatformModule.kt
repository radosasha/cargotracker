package com.shiplocate.di

import com.shiplocate.data.datasource.FirebaseTokenService
import com.shiplocate.data.datasource.FirebaseTokenServiceAdapter
import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import com.shiplocate.domain.usecase.logs.SendAllLogsUseCase
import com.shiplocate.domain.usecase.logs.SendLogsUseCase
import org.koin.dsl.module

/**
 * iOS платформо-специфичный модуль для composeApp
 */
val iosPlatformModule = module {

    // Регистрируем actual класс FirebaseTokenServiceDataSource для iOS
    single<FirebaseTokenServiceDataSource> { FirebaseTokenServiceDataSource(get()) }
    
    // Регистрируем адаптер для связи с интерфейсом из data модуля
    single<FirebaseTokenService> { FirebaseTokenServiceAdapter(get()) }
    
    // Регистрируем SendLogsUseCase для iOS
    single<SendLogsUseCase> { 
        SendLogsUseCase(get(), get()) 
    }
    single<SendAllLogsUseCase> { SendAllLogsUseCase() }
}
