package com.tracker.data.di

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tracker.data.datasource.DeviceDataSource
import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.PrefsDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.GpsLocationDataSourceImpl
import com.tracker.data.datasource.impl.IOSDeviceDataSource
import com.tracker.data.datasource.impl.IOSGpsManager
import com.tracker.data.datasource.impl.IOSPermissionDataSource
import com.tracker.data.datasource.impl.PrefsDataSourceImpl
import com.tracker.data.datasource.impl.IOSTrackingDataSource
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для data слоя
 */
val iosDataModule = module {
    
    // SQLite Driver для iOS
    single<SQLiteDriver> { BundledSQLiteDriver() }
    
    // iOS-specific Data Sources
    single<GpsManager> { IOSGpsManager() }
    single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
    single<DeviceDataSource> { IOSDeviceDataSource() }
    single<PermissionDataSource> { IOSPermissionDataSource(get()) }
    single<TrackingDataSource> { IOSTrackingDataSource(get()) }
    single<PrefsDataSource> { PrefsDataSourceImpl(get()) }
}