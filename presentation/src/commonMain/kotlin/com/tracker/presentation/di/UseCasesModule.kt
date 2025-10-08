package com.tracker.presentation.di

import com.tracker.domain.usecase.GetPermissionStatusUseCase
import com.tracker.domain.usecase.GetTrackingStatusUseCase
import com.tracker.domain.usecase.RequestAllPermissionsUseCase
import com.tracker.domain.usecase.StartProcessLocationsUseCase
import com.tracker.domain.usecase.StartTrackingUseCase
import com.tracker.domain.usecase.StopProcessLocationsUseCase
import com.tracker.domain.usecase.StopTrackingUseCase
import com.tracker.domain.usecase.TestServerUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Модуль для регистрации Use Cases (Domain слой)
 * 
 * Use Cases регистрируются здесь, а не в domain модуле,
 * чтобы domain оставался независимым от DI framework (Koin)
 * 
 * Factory scope - каждый Use Case создается заново при каждом использовании
 */
val useCasesModule = module {
    
    // Permission Use Cases
    factoryOf(::GetPermissionStatusUseCase)
    factoryOf(::RequestAllPermissionsUseCase)
    
    // Tracking Use Cases
    factoryOf(::GetTrackingStatusUseCase)
    factoryOf(::StartTrackingUseCase)
    factoryOf(::StopTrackingUseCase)
    
    // Location Processing Use Cases
    factoryOf(::StartProcessLocationsUseCase)
    factoryOf(::StopProcessLocationsUseCase)
    
    // Server Use Cases
    factoryOf(::TestServerUseCase)
}
