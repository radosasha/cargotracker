package com.shiplocate.data.di

import com.shiplocate.core.database.DatabaseProvider
import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.datastore.DataStoreProvider
import com.shiplocate.core.network.HttpClientProvider
import com.shiplocate.data.config.ServerConfig
import com.shiplocate.data.datasource.impl.FirebaseTokenLocalDataSourceImpl
import com.shiplocate.data.datasource.FirebaseTokenRemoteDataSource
import com.shiplocate.data.datasource.impl.FirebaseTokenRemoteDataSourceImpl
import com.shiplocate.data.datasource.GpsLocationDataSource
import com.shiplocate.data.datasource.LocationLocalDataSource
import com.shiplocate.data.datasource.LocationRemoteDataSource
import com.shiplocate.data.datasource.PrefsDataSource
import com.shiplocate.data.datasource.TrackingDataSource
import com.shiplocate.data.datasource.impl.GpsLocationDataSourceImpl
import com.shiplocate.data.datasource.impl.LocationLocalDataSourceImpl
import com.shiplocate.data.datasource.impl.LocationRemoteDataSourceImpl
import com.shiplocate.data.datasource.impl.PrefsDataSourceImpl
import com.shiplocate.data.datasource.impl.TrackingDataSourceImpl
import com.shiplocate.data.datasource.load.LoadLocalDataSource
import com.shiplocate.data.datasource.load.LoadRemoteDataSource
import com.shiplocate.data.datasource.remote.AuthRemoteDataSource
import com.shiplocate.data.network.api.AuthApi
import com.shiplocate.data.network.api.AuthApiImpl
import com.shiplocate.data.network.api.FirebaseTokenApi
import com.shiplocate.data.network.api.FirebaseTokenApiImpl
import com.shiplocate.data.network.api.FlespiLocationApi
import com.shiplocate.data.network.api.LoadApi
import com.shiplocate.data.network.api.LoadApiImpl
import com.shiplocate.data.network.api.LogsApi
import com.shiplocate.data.network.api.LogsApiImpl
import com.shiplocate.data.datasource.LogsRemoteDataSource
import com.shiplocate.data.datasource.impl.LogsRemoteDataSourceImpl
import com.shiplocate.data.network.api.OsmAndLocationApi
import com.shiplocate.data.repository.AuthPreferencesRepositoryImpl
import com.shiplocate.data.repository.AuthRepositoryImpl
import com.shiplocate.data.repository.DeviceRepositoryImpl
import com.shiplocate.data.repository.LoadRepositoryImpl
import com.shiplocate.data.repository.LocationRepositoryImpl
import com.shiplocate.data.repository.LogsRepositoryImpl
import com.shiplocate.data.repository.NotificationRepositoryImpl
import com.shiplocate.data.repository.PermissionRepositoryImpl
import com.shiplocate.data.repository.PrefsRepositoryImpl
import com.shiplocate.data.repository.TrackingRepositoryImpl
import com.shiplocate.data.services.LocationProcessorImpl
import com.shiplocate.data.services.LocationSyncServiceImpl
import com.shiplocate.domain.datasource.FirebaseTokenLocalDataSource
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.DeviceRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.LocationRepository
import com.shiplocate.domain.repository.LogsRepository
import com.shiplocate.domain.repository.NotificationRepository
import com.shiplocate.domain.repository.PermissionRepository
import com.shiplocate.domain.repository.PrefsRepository
import com.shiplocate.domain.repository.TrackingRepository
import com.shiplocate.domain.service.LocationProcessor
import com.shiplocate.domain.service.LocationSyncService
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
            single { get<DataStoreProvider>().createDataStore(fileName = "shiplocate.preferences_pb") }

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
            single { OsmAndLocationApi(get(), ServerConfig.OSMAND_SERVER_URL, get()) }
            single { FlespiLocationApi(get(), ServerConfig.FLESPI_SERVER_URL, get()) }

            // Auth API
            single<AuthApi> {
                AuthApiImpl(
                    httpClient = get(),
                    baseUrl = "http://${ServerConfig.BASE_URL}:8082",
                )
            }

            // Firebase Token API
            single<FirebaseTokenApi> {
                FirebaseTokenApiImpl(
                    httpClient = get(),
                    baseUrl = "http://${ServerConfig.BASE_URL}:8082",
                )
            }

            // Load API
            single<LoadApi> {
                LoadApiImpl(
                    httpClient = get(),
                    baseUrl = "http://${ServerConfig.BASE_URL}:8082",
                    logger = get(),
                )
            }

            // Logs API
            single<LogsApi> {
                LogsApiImpl(
                    httpClient = get(),
                    baseUrl = "http://${ServerConfig.BASE_URL}:8082",
                    filesManager = get(),
                    logger = get(),
                )
            }

            // Data Sources
            single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
            single<LocationRemoteDataSource> { LocationRemoteDataSourceImpl(get(), get(), get()) }
            single<TrackingDataSource> { TrackingDataSourceImpl(get()) }
            single<PrefsDataSource> { PrefsDataSourceImpl(get()) }
            single<AuthRemoteDataSource> { AuthRemoteDataSource(get(), get()) }
            single { LoadRemoteDataSource(get()) }
            single { LoadLocalDataSource(get()) }
            single<LogsRemoteDataSource> { LogsRemoteDataSourceImpl(get(), get()) }

            // Firebase Token Data Sources
            single<FirebaseTokenLocalDataSource> { FirebaseTokenLocalDataSourceImpl(get()) }
            single<FirebaseTokenRemoteDataSource> { FirebaseTokenRemoteDataSourceImpl(get(), get()) }
            // FirebaseTokenService регистрируется в платформо-специфичных модулях composeApp

            // Repositories
            single<DeviceRepository> { DeviceRepositoryImpl(get()) }
            single<LocationRepository> { LocationRepositoryImpl(get(), get(), get()) }
            single<PermissionRepository> { PermissionRepositoryImpl(get()) }
            single<PrefsRepository> { PrefsRepositoryImpl(get()) }
            single<TrackingRepository> { TrackingRepositoryImpl(get()) }
            single<AuthRepository> { AuthRepositoryImpl(get()) }
            single<AuthPreferencesRepository> { AuthPreferencesRepositoryImpl(get(), get()) }
            single<LoadRepository> { LoadRepositoryImpl(get(), get(), get()) }
            single<LogsRepository> { LogsRepositoryImpl(get(), get(), get()) }
            single<NotificationRepository> { NotificationRepositoryImpl(get(), get(), get(), get()) }

            // Domain Services - реализации в data слое
            single<LocationProcessor> { LocationProcessorImpl() }
            single<LocationSyncService> { LocationSyncServiceImpl(get(), get(), get(), get()) }

            // CoroutineScope для LocationSyncService
            single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
        }
