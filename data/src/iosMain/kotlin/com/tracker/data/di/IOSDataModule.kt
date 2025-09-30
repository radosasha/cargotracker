package com.tracker.data.di

import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.IOSPermissionDataSource
import com.tracker.data.datasource.impl.IOSTrackingDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * iOS-специфичный Data модуль
 */
val iosDataModule = module {
    
    // HTTP Client с iOS-специфичными настройками
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            install(Logging) {
                level = LogLevel.INFO
            }
            
            // iOS engine настройки
            engine {
                // Здесь можно добавить iOS-специфичные настройки
            }
        }
    }
    
    // iOS-специфичные Data Sources
    single<PermissionDataSource> { IOSPermissionDataSource() }
    single<TrackingDataSource> { IOSTrackingDataSource() }
}
