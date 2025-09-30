package com.tracker.data.di

import com.tracker.data.datasource.LocationDataSource
import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.LocalLocationDataSource
import com.tracker.data.datasource.impl.RemoteLocationDataSource
import com.tracker.data.repository.LocationRepositoryImpl
import com.tracker.data.repository.PermissionRepositoryImpl
import com.tracker.data.repository.TrackingRepositoryImpl
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.TrackingRepository
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Data модуль с реализациями репозиториев, data sources и use cases
 */
val dataModule = module {
    
    // HTTP Client
    single<HttpClient> {
        HttpClient {
            // Конфигурация будет в платформо-специфичных модулях
        }
    }
    
    // Data Sources
    single<LocationDataSource> { LocalLocationDataSource() }
    single<LocationRemoteDataSource> { RemoteLocationDataSource(get()) }
    // PermissionDataSource и TrackingDataSource будут реализованы в платформо-специфичных модулях
    
    // Repositories
    single<LocationRepository> { LocationRepositoryImpl(get(), get()) }
    single<PermissionRepository> { PermissionRepositoryImpl(get()) }
    single<TrackingRepository> { TrackingRepositoryImpl(get()) }
    
}
