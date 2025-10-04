package com.tracker.data.di

import androidx.room.RoomDatabase
import com.tracker.data.config.DeviceConfig
import com.tracker.data.config.ServerConfig
import com.tracker.data.database.TrackerDatabase
import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.datasource.impl.LocationRemoteDataSourceImpl
import com.tracker.data.network.api.OsmAndLocationApi
import com.tracker.data.network.api.FlespiLocationApi
import com.tracker.data.repository.DeviceRepositoryImpl
import com.tracker.data.repository.LocationRepositoryImpl
import com.tracker.data.repository.PermissionRepositoryImpl
import com.tracker.data.repository.TrackingRepositoryImpl
import com.tracker.domain.repository.DeviceRepository
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.TrackingRepository
import org.koin.dsl.module

/**
 * Data модуль с реализациями репозиториев и data sources
 */
val dataModule = module {
    
    // Network API
    single { OsmAndLocationApi(get(), ServerConfig.SERVER_URL, DeviceConfig.DEVICE_ID) }
    single { FlespiLocationApi(get(), ServerConfig.SERVER_URL, DeviceConfig.DEVICE_ID) }
    
    // Data Sources
    single<LocationRemoteDataSource> { LocationRemoteDataSourceImpl(get(), get()) }
    
    // Device ID
    single { DeviceConfig.DEVICE_ID }
    
    // Repositories
    single<DeviceRepository> { DeviceRepositoryImpl(get()) }
    single<LocationRepository> { LocationRepositoryImpl(get(), get(), get(), get()) }
    single<PermissionRepository> { PermissionRepositoryImpl(get()) }
    single<TrackingRepository> { TrackingRepositoryImpl(get()) }
    
}

/**
 * Функция для создания Room Database билдера (должна быть реализована на платформах)
 */
expect fun getRoomDatabaseBuilder(): RoomDatabase.Builder<TrackerDatabase>
