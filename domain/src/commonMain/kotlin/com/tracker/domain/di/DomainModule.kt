package com.tracker.domain.di

import com.tracker.domain.usecase.GetPermissionStatusUseCase
import com.tracker.domain.usecase.GetRecentLocationsUseCase
import com.tracker.domain.usecase.GetTrackingStatusUseCase
import com.tracker.domain.usecase.RequestAllPermissionsUseCase
import com.tracker.domain.usecase.StartTrackingUseCase
import com.tracker.domain.usecase.StopTrackingUseCase
import com.tracker.domain.usecase.SyncLocationsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Domain модуль с Use Cases
 */
val domainModule = module {
    
    // Use Cases
    factoryOf(::GetPermissionStatusUseCase)
    factoryOf(::GetTrackingStatusUseCase)
    factoryOf(::RequestAllPermissionsUseCase)
    factoryOf(::StartTrackingUseCase)
    factoryOf(::StopTrackingUseCase)
    factoryOf(::GetRecentLocationsUseCase)
    factoryOf(::SyncLocationsUseCase)
}
