package com.tracker.data.di

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tracker.data.config.DeviceConfig
import com.tracker.core.database.TrackerDatabase
import com.tracker.core.database.createDatabaseBuilder
import com.tracker.data.datasource.DeviceDataSource
import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.datasource.LocationLocalDataSource
import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.PrefsDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.core.datastore.DataStoreProvider
import com.tracker.data.datasource.impl.GpsLocationDataSourceImpl
import com.tracker.data.datasource.impl.IOSDeviceDataSource
import com.tracker.data.datasource.impl.IOSGpsManager
import com.tracker.data.datasource.impl.IOSPermissionDataSource
import com.tracker.data.datasource.impl.LocationLocalDataSourceImpl
import com.tracker.data.datasource.impl.PrefsDataSourceImpl
import com.tracker.data.network.api.OsmAndLocationApi
import com.tracker.data.network.api.FlespiLocationApi
import com.tracker.data.config.ServerConfig
import com.tracker.data.datasource.impl.IOSTrackingDataSource
import com.tracker.data.datasource.impl.LocationRemoteDataSourceImpl
import com.tracker.core.network.HttpClientProvider
import com.tracker.data.repository.LocationRepositoryImpl
import com.tracker.data.repository.PermissionRepositoryImpl
import com.tracker.data.repository.TrackingRepositoryImpl
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.TrackingRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для data слоя
 */
val iosDataModule = module {
    
    // HTTP Client для iOS
    single<HttpClient> { HttpClientProvider().createHttpClient() }
    
    // DataStore
    single<DataStoreProvider> { DataStoreProvider() }
    single { get<DataStoreProvider>().createDataStore() }
    
    // SQLite Driver для iOS
    single<SQLiteDriver> { BundledSQLiteDriver() }
    
    // Database - создается через core модуль
    single<TrackerDatabase> { 
        createDatabaseBuilder()
            .setDriver(get())
            .build()
    }
    
    // Local Data Source (Room)
    single<LocationLocalDataSource> { LocationLocalDataSourceImpl(get()) }
    
    // iOS-specific Data Sources
    single<GpsManager> { IOSGpsManager() }
    single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
    single<DeviceDataSource> { IOSDeviceDataSource() }
    single<PermissionDataSource> { IOSPermissionDataSource(get()) }
    single<TrackingDataSource> { IOSTrackingDataSource(get()) }
    single<PrefsDataSource> { PrefsDataSourceImpl(get()) }
}