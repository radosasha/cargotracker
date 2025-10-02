package com.tracker.data.di

import com.tracker.data.database.TrackerDatabase
import com.tracker.data.datasource.LocalLocationDataSource
import com.tracker.data.datasource.LocationDataSource
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.PermissionChecker
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.IOSPermissionDataSource
import com.tracker.data.datasource.impl.IOSTrackingDataSource
import com.tracker.data.datasource.impl.RoomLocalLocationDataSource
import com.tracker.data.model.LocationDataModel
import com.tracker.data.network.client.HttpClientProvider
import com.tracker.data.repository.LocationRepositoryImpl
import com.tracker.data.repository.PermissionRepositoryImpl
import com.tracker.data.repository.TrackingRepositoryImpl
import com.tracker.domain.datasource.LocationManager
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
    
    // Room Database
    single<TrackerDatabase> {
        getRoomDatabaseBuilder()
            .setDriver(get())
            .build()
    }
    
    // Local Data Source (Room)
    single<LocalLocationDataSource> { RoomLocalLocationDataSource(get()) }
    
    // PermissionChecker - заглушка для iOS
    single<PermissionChecker> { 
        object : PermissionChecker {
            override suspend fun hasLocationPermissions(): Boolean = false
            override suspend fun hasBackgroundLocationPermission(): Boolean = false
            override suspend fun hasNotificationPermission(): Boolean = false
            override suspend fun hasAllRequiredPermissions(): Boolean = false
            override suspend fun getPermissionStatusMessage(): String = "Permissions not implemented"
            override suspend fun openAppSettings(): Result<Unit> = Result.success(Unit)
            override fun requestAllPermissions() {}
        }
    }
    
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
    
    // LocationManager - заглушка для iOS с отслеживанием состояния
    single<LocationManager> {
        object : LocationManager {
            private var isTracking = false
            
            override fun startLocationTracking(): Result<Unit> {
                isTracking = true
                println("IOS LocationManager: Tracking started (simulated)")
                return Result.success(Unit)
            }
            
            override fun stopLocationTracking(): Result<Unit> {
                isTracking = false
                println("IOS LocationManager: Tracking stopped (simulated)")
                return Result.success(Unit)
            }
            
            override fun isLocationTrackingActive(): Boolean {
                println("IOS LocationManager: isTrackingActive() = $isTracking")
                return isTracking
            }
            
            override fun observeLocationUpdates(): Flow<com.tracker.domain.model.Location> = flowOf()
        }
    }
    
    // iOS-specific Data Sources
    single<PermissionDataSource> { IOSPermissionDataSource(get()) }
    single<TrackingDataSource> { IOSTrackingDataSource() }
    
    // Repositories
    single<PermissionRepository> { PermissionRepositoryImpl(get()) }
    single<TrackingRepository> { TrackingRepositoryImpl(get()) }
    single<LocationRepository> { LocationRepositoryImpl(get(), get(), get()) }
}