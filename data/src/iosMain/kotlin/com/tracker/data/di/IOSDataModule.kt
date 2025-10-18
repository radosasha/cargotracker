package com.tracker.data.di

import com.tracker.core.database.DatabaseProvider
import com.tracker.core.datastore.DataStoreProvider
import com.tracker.data.datasource.DeviceDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.impl.IOSDeviceDataSource
import com.tracker.data.datasource.impl.IOSGpsManager
import com.tracker.data.datasource.impl.IOSPermissionDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для data слоя
 */
actual val platformDataModule: Module =
    module {

        // DataStore Provider для iOS (не требует Context)
        single<DataStoreProvider> { DataStoreProvider() }

        // Database Provider для iOS (не требует Context)
        single<DatabaseProvider> { DatabaseProvider() }

        // iOS-specific Data Sources
        single<GpsManager> { IOSGpsManager() }
        single<DeviceDataSource> { IOSDeviceDataSource() }
        single<PermissionDataSource> { IOSPermissionDataSource(get()) }
    }
