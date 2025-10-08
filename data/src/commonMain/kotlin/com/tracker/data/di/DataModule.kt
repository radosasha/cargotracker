package com.tracker.data.di

import com.tracker.core.database.DatabaseProvider
import com.tracker.core.database.TrackerDatabase
import com.tracker.core.datastore.DataStoreProvider
import com.tracker.core.network.HttpClientProvider
import com.tracker.data.config.DeviceConfig
import com.tracker.data.config.ServerConfig
import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.LocationLocalDataSource
import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.datasource.PrefsDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.GpsLocationDataSourceImpl
import com.tracker.data.datasource.impl.LocationLocalDataSourceImpl
import com.tracker.data.datasource.impl.LocationRemoteDataSourceImpl
import com.tracker.data.datasource.impl.PrefsDataSourceImpl
import com.tracker.data.datasource.impl.TrackingDataSourceImpl
import com.tracker.data.network.api.FlespiLocationApi
import com.tracker.data.network.api.OsmAndLocationApi
import com.tracker.domain.service.LocationProcessor
import com.tracker.domain.service.LocationSyncService
import com.tracker.data.repository.DeviceRepositoryImpl
import com.tracker.data.repository.LocationRepositoryImpl
import com.tracker.data.repository.PermissionRepositoryImpl
import com.tracker.data.repository.PrefsRepositoryImpl
import com.tracker.data.repository.TrackingRepositoryImpl
import com.tracker.domain.repository.DeviceRepository
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.PrefsRepository
import com.tracker.domain.repository.TrackingRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Data модуль с реализациями репозиториев и data sources
 * Содержит только общие зависимости, не зависящие от платформы
 */
val dataModule = module {

    // HTTP Client
    single<HttpClient> { HttpClientProvider().createHttpClient() }

    // DataStore - создается через DataStoreProvider (определен в платформо-специфичных модулях)
    single { get<DataStoreProvider>().createDataStore(fileName = "tracker.preferences_pb") }

    // Database - создается через DatabaseProvider
    single<TrackerDatabase> {
        get<DatabaseProvider>().createDatabase(databaseName = TrackerDatabase.DATABASE_NAME)
    }

    // Local Data Source (Room)
    single<LocationLocalDataSource> { LocationLocalDataSourceImpl(get()) }

    // Network API
    single { OsmAndLocationApi(get(), ServerConfig.OSMAND_SERVER_URL, DeviceConfig.DEVICE_ID) }
    single { FlespiLocationApi(get(), ServerConfig.FLESPI_SERVER_URL, DeviceConfig.DEVICE_ID) }

    // Data Sources
    single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
    single<LocationRemoteDataSource> { LocationRemoteDataSourceImpl(get(), get()) }
    single<TrackingDataSource> { TrackingDataSourceImpl(get()) }
    single<PrefsDataSource> { PrefsDataSourceImpl(get()) }

    // Device ID
    single { DeviceConfig.DEVICE_ID }

    // Repositories
    single<DeviceRepository> { DeviceRepositoryImpl(get()) }
    single<LocationRepository> { LocationRepositoryImpl(get(), get(), get(), get()) }
    single<PermissionRepository> { PermissionRepositoryImpl(get()) }
    single<PrefsRepository> { PrefsRepositoryImpl(get()) }
    single<TrackingRepository> { TrackingRepositoryImpl(get()) }
    
    // Domain Services (перенесены из domain модуля)
    single { LocationProcessor() }
    single { LocationSyncService(get(), get()) }
    
    // CoroutineScope для LocationSyncService
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

}

