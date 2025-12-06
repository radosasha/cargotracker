package com.shiplocate.data.di

import com.shiplocate.core.database.DatabaseProvider
import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.datastore.DataStoreProvider
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.network.HttpClientProvider
import com.shiplocate.data.config.ServerConfig
import com.shiplocate.data.datasource.FirebaseTokenRemoteDataSource
import com.shiplocate.data.datasource.GpsLocationDataSource
import com.shiplocate.data.datasource.LocationLocalDataSource
import com.shiplocate.data.datasource.LocationRemoteDataSource
import com.shiplocate.data.datasource.LogsRemoteDataSource
import com.shiplocate.data.datasource.PrefsDataSource
import com.shiplocate.data.datasource.TrackingDataSource
import com.shiplocate.data.datasource.auth.AuthPreferences
import com.shiplocate.data.datasource.firebase.FirebasePreferences
import com.shiplocate.data.datasource.impl.FirebaseTokenLocalDataSourceImpl
import com.shiplocate.data.datasource.impl.FirebaseTokenRemoteDataSourceImpl
import com.shiplocate.data.datasource.impl.GpsLocationDataSourceImpl
import com.shiplocate.data.datasource.impl.LocationLocalDataSourceImpl
import com.shiplocate.data.datasource.impl.LocationRemoteDataSourceImpl
import com.shiplocate.data.datasource.impl.LogsRemoteDataSourceImpl
import com.shiplocate.data.datasource.impl.PrefsDataSourceImpl
import com.shiplocate.data.datasource.impl.RouteLocalDataSourceImpl
import com.shiplocate.data.datasource.impl.TrackingDataSourceImpl
import com.shiplocate.data.datasource.load.LoadsLocalDataSource
import com.shiplocate.data.datasource.load.LoadsRemoteDataSource
import com.shiplocate.data.datasource.load.RouteLocalDataSource
import com.shiplocate.data.datasource.load.StopsLocalDataSource
import com.shiplocate.data.datasource.message.MessagesLocalDataSource
import com.shiplocate.data.datasource.message.MessagesRemoteDataSource
import com.shiplocate.data.datasource.remote.AuthRemoteDataSource
import com.shiplocate.data.datasource.route.RoutePreferences
import com.shiplocate.data.network.api.AuthApi
import com.shiplocate.data.network.api.AuthApiImpl
import com.shiplocate.data.network.api.FirebaseTokenApi
import com.shiplocate.data.network.api.FirebaseTokenApiImpl
import com.shiplocate.data.network.api.LoadApi
import com.shiplocate.data.network.api.LoadApiImpl
import com.shiplocate.data.network.api.LocationApi
import com.shiplocate.data.network.api.LogsApi
import com.shiplocate.data.network.api.LogsApiImpl
import com.shiplocate.data.repository.AuthRepositoryImpl
import com.shiplocate.data.repository.DeviceRepositoryImpl
import com.shiplocate.data.repository.GpsRepositoryImpl
import com.shiplocate.data.repository.LoadRepositoryImpl
import com.shiplocate.data.repository.LocationRepositoryImpl
import com.shiplocate.data.repository.LogsRepositoryImpl
import com.shiplocate.data.repository.MessagesRepositoryImpl
import com.shiplocate.data.repository.NotificationRepositoryImpl
import com.shiplocate.data.repository.PermissionRepositoryImpl
import com.shiplocate.data.repository.RouteRepositoryImpl
import com.shiplocate.data.repository.TrackingRepositoryImpl
import com.shiplocate.data.services.LocationProcessorImpl
import com.shiplocate.data.services.LocationSyncServiceImpl
import com.shiplocate.domain.datasource.FirebaseTokenLocalDataSource
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.DeviceRepository
import com.shiplocate.domain.repository.GpsRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.LocationRepository
import com.shiplocate.domain.repository.LogsRepository
import com.shiplocate.domain.repository.MessagesRepository
import com.shiplocate.domain.repository.NotificationRepository
import com.shiplocate.domain.repository.PermissionRepository
import com.shiplocate.domain.repository.RouteRepository
import com.shiplocate.domain.repository.TrackingRepository
import com.shiplocate.domain.service.LocationProcessor
import com.shiplocate.domain.service.LocationSyncService
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

            // AuthPreferences - создается через DataStoreProvider (определен в платформо-специфичных модулях)
            single<AuthPreferences> { AuthPreferences(get<DataStoreProvider>().createDataStore(fileName = "shiplocate.preferences_pb")) }
            single<RoutePreferences> { RoutePreferences(get<DataStoreProvider>().createDataStore(fileName = "shiplocate_route.preferences_pb")) }
            single<FirebasePreferences> { FirebasePreferences(get<DataStoreProvider>().createDataStore(fileName = "shiplocate_firebase.preferences_pb")) }

            // Database - создается через DatabaseProvider (определен в платформо-специфичных модулях)
            single<TrackerDatabase> {
                get<DatabaseProvider>().createDatabase(databaseName = TrackerDatabase.DATABASE_NAME)
            }

            // Local Data Source (Room)
            single<LocationLocalDataSource> { LocationLocalDataSourceImpl(get()) }

            // JSON serialization
            single<Json> {
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            }

            val baseUrl = "http://${ServerConfig.BASE_URL}"
            // Network API
            single {
                LocationApi(
                    httpClient = get(),
                    baseUrl = baseUrl,
//                    baseUrl = "https://${ServerConfig.BASE_URL}",
                    logger = get(),
                )
            }

            // Auth API
            single<AuthApi> {
                AuthApiImpl(
                    httpClient = get(),
                    baseUrl = baseUrl,
//                    baseUrl = "https://${ServerConfig.BASE_URL}",
                )
            }

            // Firebase Token API
            single<FirebaseTokenApi> {
                FirebaseTokenApiImpl(
                    httpClient = get(),
                    baseUrl = baseUrl,
//                    baseUrl = "https://${ServerConfig.BASE_URL}",
                )
            }

            // Load API
            single<LoadApi> {
                LoadApiImpl(
                    httpClient = get(),
                    baseUrl = baseUrl,
//                    baseUrl = "https://${ServerConfig.BASE_URL}",
                    logger = get(),
                )
            }

            // Logs API
            single<LogsApi> {
                LogsApiImpl(
                    httpClient = get(),
                    baseUrl = baseUrl,
//                    baseUrl = "https://${ServerConfig.BASE_URL}",
                    filesManager = get(),
                    logger = get(),
                )
            }

            // Data Sources
            single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
            single<LocationRemoteDataSource> { LocationRemoteDataSourceImpl(get(), get()) }
            single<TrackingDataSource> { TrackingDataSourceImpl(get()) }
            single<PrefsDataSource> { PrefsDataSourceImpl(get()) }
            single<AuthRemoteDataSource> { AuthRemoteDataSource(get(), get()) }
            single { LoadsRemoteDataSource(get()) }
            single { StopsLocalDataSource(get()) }
            single { LoadsLocalDataSource(get(), get<StopsLocalDataSource>()) }
            single<RouteLocalDataSource> { RouteLocalDataSourceImpl(get<RoutePreferences>(), get<Json>(), get()) }
            single { MessagesRemoteDataSource(get()) }
            single { MessagesLocalDataSource(get()) }
            single<LogsRemoteDataSource> { LogsRemoteDataSourceImpl(get(), get()) }

            // Firebase Token Data Sources
            single<FirebaseTokenLocalDataSource> { FirebaseTokenLocalDataSourceImpl(get<FirebasePreferences>()) }
            single<FirebaseTokenRemoteDataSource> { FirebaseTokenRemoteDataSourceImpl(get(), get(), get()) }
            // FirebaseTokenService регистрируется в платформо-специфичных модулях composeApp

            // Repositories
            single<GpsRepository> { GpsRepositoryImpl(get()) }
            single<DeviceRepository> { DeviceRepositoryImpl(get()) }
            single<LocationRepository> { LocationRepositoryImpl(get(), get()) }
            single<PermissionRepository> { PermissionRepositoryImpl(get()) }
            single<RouteRepository> {
                RouteRepositoryImpl(
                    routeLocalDataSource = get<RouteLocalDataSource>(),
                    loadsRemoteDataSource = get(),
                    logger = get<Logger>(),
                )
            }
            single<TrackingRepository> { TrackingRepositoryImpl(get()) }
            single<AuthRepository> { AuthRepositoryImpl(get<AuthPreferences>(), get(), get()) }
            single<LoadRepository> { LoadRepositoryImpl(get(), get(), get(), get()) }
            single<MessagesRepository> { MessagesRepositoryImpl(get(), get(), get(), get()) }
            single<LogsRepository> { LogsRepositoryImpl(get(), get(), get()) }
            single<NotificationRepository> { NotificationRepositoryImpl(get(), get(), get(), get()) }

            // Domain Services - реализации в data слое
            single<LocationProcessor> {
                LocationProcessorImpl(
                    minSendIntervalMs = 60 * 1000L,
                    minDistanceForSendM = 50f,
                    maxAccuracyM = 70f,
                    forceSaveIntervalMs = 30 * 60 * 1000L
                )
            }
            single<LocationSyncService> { LocationSyncServiceImpl(get(), get(), get(), get(), get()) }

            // CoroutineScope для LocationSyncService
            factory<CoroutineScope> { CoroutineScope(Dispatchers.Default) }
        }
