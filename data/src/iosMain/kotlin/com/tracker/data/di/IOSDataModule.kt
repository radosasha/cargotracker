package com.tracker.data.di

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tracker.data.config.DeviceConfig
import com.tracker.data.database.TrackerDatabase
import com.tracker.data.datasource.LocalLocationDataSource
import com.tracker.data.datasource.LocationDataSource
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.IOSPermissionDataSource
import com.tracker.data.datasource.impl.IOSTrackingDataSource
import com.tracker.data.datasource.impl.RoomLocalLocationDataSource
import com.tracker.data.model.LocationDataModel
import com.tracker.data.network.client.HttpClientProvider
import com.tracker.data.repository.LocationRepositoryImpl
import com.tracker.data.repository.PermissionRepositoryImpl
import com.tracker.data.repository.TrackingRepositoryImpl
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.TrackingRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для data слоя
 */
val iosDataModule = module {
    
    // HTTP Client для iOS
    single<HttpClient> { HttpClientProvider().createHttpClient() }
    
    // SQLite Driver для iOS
    single<SQLiteDriver> { BundledSQLiteDriver() }
    
    // Room Database
    single<TrackerDatabase> {
        getRoomDatabaseBuilder()
            .setDriver(get())
            .build()
    }
    
    // Local Data Source (Room)
    single<LocalLocationDataSource> { RoomLocalLocationDataSource(get()) }
    
    
    // LocationDataSource - заглушка для iOS
    single<LocationDataSource> {
        object : LocationDataSource {
            override suspend fun saveLocation(location: LocationDataModel) {}
            override suspend fun getAllLocations(): List<LocationDataModel> = emptyList()
            override suspend fun getRecentLocations(limit: Int): List<LocationDataModel> = emptyList()
            override suspend fun clearOldLocations(olderThanDays: Int) {}
            override fun observeLocations(): Flow<LocationDataModel> = flowOf()
        }
    }
    
    
    // Device ID
    single { DeviceConfig.DEVICE_ID }
    
    // iOS-specific Data Sources
    single<PermissionDataSource> { IOSPermissionDataSource(get()) }
    single<TrackingDataSource> { IOSTrackingDataSource() }
    
    // Repositories
    single<PermissionRepository> { PermissionRepositoryImpl(get()) }
    single<TrackingRepository> { TrackingRepositoryImpl(get()) }
    single<LocationRepository> { LocationRepositoryImpl(get(), get(), get()) }
}