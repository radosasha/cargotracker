package com.tracker.data.di

import com.tracker.data.database.TrackerDatabase
import com.tracker.data.datasource.DeviceDataSource
import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.datasource.LocationLocalDataSource
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.AndroidDeviceDataSource
import com.tracker.data.datasource.impl.AndroidGpsManager
import com.tracker.data.datasource.impl.AndroidPermissionDataSource
import com.tracker.data.datasource.impl.AndroidTrackingDataSource
import com.tracker.data.datasource.impl.GpsLocationDataSourceImpl
import com.tracker.data.datasource.impl.LocationLocalDataSourceImpl
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
    single<LocationLocalDataSource> { LocationLocalDataSourceImpl(get()) }
    
    // Android-specific Data Sources
    single<GpsManager> { AndroidGpsManager(get()) }
    single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
    single<DeviceDataSource> { AndroidDeviceDataSource(get()) }
    single<PermissionDataSource> { AndroidPermissionDataSource() }
    single<TrackingDataSource> { AndroidTrackingDataSource() }
    
}