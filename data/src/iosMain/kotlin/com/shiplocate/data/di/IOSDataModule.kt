package com.shiplocate.data.di

import com.shiplocate.core.database.DatabaseProvider
import com.shiplocate.core.datastore.DataStoreProvider
import com.shiplocate.data.datasource.DeviceDataSource
import com.shiplocate.data.datasource.GpsManager
import com.shiplocate.data.datasource.LogsLocalDataSource
import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.impl.IOSDeviceDataSource
import com.shiplocate.data.datasource.impl.IOSGpsManager
import com.shiplocate.data.datasource.impl.IOSPermissionDataSource
import com.shiplocate.data.datasource.impl.LogsLocalDataSourceImpl
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
        single<LogsLocalDataSource> { LogsLocalDataSourceImpl(get(), get(), get(), get()) }
    }
