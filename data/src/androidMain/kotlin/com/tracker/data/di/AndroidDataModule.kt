package com.tracker.data.di

import com.tracker.data.datasource.DeviceDataSource
import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.datasource.PrefsDataSource
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.datasource.impl.AndroidDeviceDataSource
import com.tracker.data.datasource.impl.AndroidGpsManager
import com.tracker.data.datasource.impl.AndroidPermissionDataSource
import com.tracker.data.datasource.impl.AndroidTrackingDataSource
import com.tracker.data.datasource.impl.GpsLocationDataSourceImpl
import com.tracker.data.datasource.impl.PrefsDataSourceImpl
import org.koin.dsl.module

/**
 * Android-специфичный модуль для data слоя
 */
val androidDataModule = module {

    // Android-specific Data Sources
    single<GpsManager> { AndroidGpsManager(get()) }
    single<GpsLocationDataSource> { GpsLocationDataSourceImpl(get()) }
    single<DeviceDataSource> { AndroidDeviceDataSource(get()) }
    single<PermissionDataSource> { AndroidPermissionDataSource(get()) }
    single<TrackingDataSource> { AndroidTrackingDataSource(get()) }
    single<PrefsDataSource> { PrefsDataSourceImpl(get()) }
    
}