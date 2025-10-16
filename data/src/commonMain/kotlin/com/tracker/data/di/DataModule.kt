package com.tracker.data.di

import com.tracker.core.database.DatabaseProvider
import com.tracker.core.database.TrackerDatabase
import com.tracker.core.datastore.DataStoreProvider
import com.tracker.core.network.HttpClientProvider
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
import com.tracker.data.datasource.load.LoadLocalDataSource
import com.tracker.data.datasource.load.LoadRemoteDataSource
import com.tracker.data.datasource.remote.AuthRemoteDataSource
import com.tracker.data.network.api.AuthApi
import com.tracker.data.network.api.AuthApiImpl
import com.tracker.data.network.api.FlespiLocationApi
import com.tracker.data.network.api.LoadApi
import com.tracker.data.network.api.LoadApiImpl
import com.tracker.data.network.api.OsmAndLocationApi
import com.tracker.data.repository.AuthPreferencesRepositoryImpl
import com.tracker.data.repository.AuthRepositoryImpl
import com.tracker.data.repository.DeviceRepositoryImpl
import com.tracker.data.repository.LoadRepositoryImpl
import com.tracker.data.repository.LocationRepositoryImpl
import com.tracker.data.repository.PermissionRepositoryImpl
import com.tracker.data.repository.PrefsRepositoryImpl
import com.tracker.data.repository.TrackingRepositoryImpl
import com.tracker.data.services.LocationProcessorImpl
import com.tracker.data.services.LocationSyncServiceImpl
import com.tracker.domain.repository.AuthPreferencesRepository
import com.tracker.domain.repository.AuthRepository
import com.tracker.domain.repository.DeviceRepository
import com.tracker.domain.repository.LoadRepository
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.PrefsRepository
import com.tracker.domain.repository.TrackingRepository
import com.tracker.domain.service.LocationProcessor
import com.tracker.domain.service.LocationSyncService
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Data модуль с реализациями репозиториев и data sources
 * Содержит только общие зависимости, не зависящие от платформы
 */
val dataModule =
    platformDataModule +
        module {

            // HTTP Client
            single<HttpClient> { HttpClientProvider().createHttpClient() }

            // DataStore - создается через DataStoreProvider (определен в платформо-специфичных модулях)
            single { get<DataStoreProvider>().createDataStore(fileName = "tracker.preferences_pb") }

            // Database - создается через DatabaseProvider (определен в платформо-специфичных модулях)
            single<TrackerDatabase> {
                get<DatabaseProvider>().createDatabase(databaseName = TrackerDatabase.DATABASE_NAME)
            }

            // Local Data Source (Room)
            single<LocationLocalDataSource> { LocationLocalDataSourceImpl(get()) }

            // JSON serialization
            single {
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            }

            // Network API
            single { OsmAndLocationApi(get(), ServerConfig.OSMAND_SERVER_URL) }
            single { FlespiLocationApi(get(), ServerConfig.FLESPI_SERVER_URL) }

            // Auth API
            single<AuthApi> {
                AuthApiImpl(
                    httpClient = get(),
                    baseUrl = "http://${ServerConfig.BASE_URL}:8082",
                )
            }

            // Load API
            single<LoadApi> {
                LoadApiImpl(
                    httpClient = get(),
                    baseUrl = "http://${ServerConfig.BASE_URL}:8082",
                )
            }

            // Data Sources
            single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
            single<LocationRemoteDataSource> { LocationRemoteDataSourceImpl(get(), get()) }
            single<TrackingDataSource> { TrackingDataSourceImpl(get()) }
            single<PrefsDataSource> { PrefsDataSourceImpl(get()) }
            single<AuthRemoteDataSource> { AuthRemoteDataSource(get(), get()) }
            single { LoadRemoteDataSource(get()) }
            single { LoadLocalDataSource(get()) }

            // Repositories
            single<DeviceRepository> { DeviceRepositoryImpl(get()) }
            single<LocationRepository> { LocationRepositoryImpl(get(), get(), get()) }
            single<PermissionRepository> { PermissionRepositoryImpl(get()) }
            single<PrefsRepository> { PrefsRepositoryImpl(get()) }
            single<TrackingRepository> { TrackingRepositoryImpl(get()) }
            single<AuthRepository> { AuthRepositoryImpl(get()) }
            single<AuthPreferencesRepository> { AuthPreferencesRepositoryImpl(get()) }
            single<LoadRepository> { LoadRepositoryImpl(get(), get()) }

            // Domain Services - реализации в data слое
            single<LocationProcessor> { LocationProcessorImpl() }
            single<LocationSyncService> { LocationSyncServiceImpl(get(), get(), get()) }

            // CoroutineScope для LocationSyncService
            single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
        }
