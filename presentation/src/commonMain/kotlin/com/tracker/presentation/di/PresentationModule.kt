package com.tracker.presentation.di

import com.tracker.domain.usecase.GetPermissionStatusUseCase
import com.tracker.domain.usecase.GetTrackingStatusUseCase
import com.tracker.domain.usecase.RequestAllPermissionsUseCase
import com.tracker.domain.usecase.StartProcessLocationsUseCase
import com.tracker.domain.usecase.StartTrackingUseCase
import com.tracker.domain.usecase.StopProcessLocationsUseCase
import com.tracker.domain.usecase.StopTrackingUseCase
import com.tracker.domain.usecase.TestServerUseCase
import com.tracker.presentation.feature.home.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Presentation модуль с ViewModels и Use Cases
 */
val presentationModule = module {
    
    // Use Cases (Factory - создаются для каждого использования)
    factoryOf(::GetPermissionStatusUseCase)
    factoryOf(::GetTrackingStatusUseCase)
    factoryOf(::RequestAllPermissionsUseCase)
    factoryOf(::StartTrackingUseCase)
    factoryOf(::StopTrackingUseCase)
    factoryOf(::StartProcessLocationsUseCase)
    factoryOf(::TestServerUseCase)
    factoryOf(::StopProcessLocationsUseCase)
    
    // ViewModels
    factoryOf(::HomeViewModel)
}
