package com.tracker.data.di

import android.content.Context
import com.tracker.core.database.TrackerDatabase
import com.tracker.core.database.createDatabaseBuilder
import com.tracker.data.datasource.DeviceDataSource
import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.datasource.LocationLocalDataSource
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.PrefsDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.core.datastore.DataStoreProvider
import com.tracker.data.datasource.impl.AndroidDeviceDataSource
import com.tracker.data.datasource.impl.AndroidGpsManager
import com.tracker.data.datasource.impl.AndroidPermissionDataSource
import com.tracker.data.datasource.impl.AndroidTrackingDataSource
import com.tracker.data.datasource.impl.GpsLocationDataSourceImpl
import com.tracker.data.datasource.impl.LocationLocalDataSourceImpl
import com.tracker.data.datasource.impl.PrefsDataSourceImpl
import com.tracker.core.network.HttpClientProvider
import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * Android-специфичный модуль для data слоя
 */
val androidDataModule = module {
    
    // HTTP Client для Android
    single<HttpClient> { HttpClientProvider().createHttpClient() }

    // DataStore
    single<DataStoreProvider> { DataStoreProvider() }
    single { get<DataStoreProvider>().createDataStore() }
    
    // Database - создается через core модуль
    single<TrackerDatabase> { 
        createDatabaseBuilder().build()
    }
    
    // Local Data Source (Room)
    single<LocationLocalDataSource> { LocationLocalDataSourceImpl(get()) }

    // Android-specific Data Sources
    single<GpsManager> { AndroidGpsManager(get()) }
    single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
    single<DeviceDataSource> { AndroidDeviceDataSource(get()) }
    single<PermissionDataSource> { AndroidPermissionDataSource(get()) }
    single<TrackingDataSource> { AndroidTrackingDataSource(get()) }
    single<PrefsDataSource> { PrefsDataSourceImpl(get()) }
    
}