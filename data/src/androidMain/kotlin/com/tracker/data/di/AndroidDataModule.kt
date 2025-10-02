package com.tracker.data.di

import com.tracker.data.database.TrackerDatabase
import com.tracker.data.datasource.LocalLocationDataSource
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.AndroidPermissionDataSource
import com.tracker.data.datasource.impl.AndroidTrackingDataSource
import com.tracker.data.datasource.impl.RoomLocalLocationDataSource
import com.tracker.data.network.client.HttpClientProvider
import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * Android-специфичный модуль для data слоя
 */
val androidDataModule = module {
    
    // HTTP Client для Android
    single<HttpClient> { HttpClientProvider().createHttpClient() }
    
    // Room Database
    single<TrackerDatabase> {
        val dbBuilder = getRoomDatabaseBuilder()
        dbBuilder.build()
    }
    
    // Local Data Source (Room)
    single<LocalLocationDataSource> { RoomLocalLocationDataSource(get()) }
    
    // Android-specific Data Sources
    single<PermissionDataSource> { AndroidPermissionDataSource() }
    single<TrackingDataSource> { AndroidTrackingDataSource() }
    
}