package com.shiplocate.data.di

import android.content.Context
import com.shiplocate.core.database.DatabaseProvider
import com.shiplocate.core.datastore.DataStoreProvider
import com.shiplocate.data.datasource.DeviceDataSource
import com.shiplocate.data.datasource.GpsManager
import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.impl.AndroidDeviceDataSource
import com.shiplocate.data.datasource.impl.AndroidGpsManager
import com.shiplocate.data.datasource.impl.AndroidPermissionDataSource
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

        // Firebase Token Service DataSource будет переопределен в composeApp модуле
    }
