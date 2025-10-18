package com.tracker.data.di

import android.content.Context
import com.tracker.core.database.DatabaseProvider
import com.tracker.core.datastore.DataStoreProvider
import com.tracker.data.datasource.DeviceDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.impl.AndroidDeviceDataSource
import com.tracker.data.datasource.impl.AndroidGpsManager
import com.tracker.data.datasource.impl.AndroidPermissionDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-специфичный модуль для data слоя
 */
actual val platformDataModule: Module =
    module {

        // DataStore Provider для Android (требует Context)
        single<DataStoreProvider> { DataStoreProvider(get<Context>()) }

        // Database Provider для Android (требует Context)
        single<DatabaseProvider> { DatabaseProvider(get<Context>()) }

        // Android-specific Data Sources
        single<GpsManager> { AndroidGpsManager(get()) }
        single<DeviceDataSource> { AndroidDeviceDataSource(get()) }
        single<PermissionDataSource> { AndroidPermissionDataSource(get()) }
    }
