package com.tracker.data.di

import android.content.Context
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.AndroidPermissionDataSource
import com.tracker.data.datasource.impl.AndroidTrackingDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Android-специфичный Data модуль
 */
val androidDataModule = module {
    
    // Android Context
    single<Context> { androidContext() }
    
    // HTTP Client с Android-специфичными настройками
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
            
            // Android engine настройки
            engine {
                // Здесь можно добавить Android-специфичные настройки
            }
        }
    }
    
    // Android-специфичные Data Sources
    single<PermissionDataSource> { AndroidPermissionDataSource() }
    single<TrackingDataSource> { AndroidTrackingDataSource() }
}