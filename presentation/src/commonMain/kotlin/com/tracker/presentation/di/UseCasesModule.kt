package com.tracker.presentation.di

import com.tracker.domain.usecase.GetPermissionStatusUseCase
import com.tracker.domain.usecase.GetTrackingStatusUseCase
import com.tracker.domain.usecase.RequestAllPermissionsUseCase
import com.tracker.domain.usecase.StartProcessLocationsUseCase
import com.tracker.domain.usecase.StartTrackingUseCase
import com.tracker.domain.usecase.StopProcessLocationsUseCase
import com.tracker.domain.usecase.StopTrackingUseCase
import com.tracker.domain.usecase.TestServerUseCase
import com.tracker.domain.usecase.auth.ClearAuthSessionUseCase
import com.tracker.domain.usecase.auth.GetAuthSessionUseCase
import com.tracker.domain.usecase.auth.HasAuthSessionUseCase
import com.tracker.domain.usecase.auth.RequestSmsCodeUseCase
import com.tracker.domain.usecase.auth.SaveAuthSessionUseCase
import com.tracker.domain.usecase.auth.VerifySmsCodeUseCase
import com.tracker.domain.usecase.load.ConnectToLoadUseCase
import com.tracker.domain.usecase.load.DisconnectFromLoadUseCase
import com.tracker.domain.usecase.load.GetCachedLoadsUseCase
import com.tracker.domain.usecase.load.GetLoadsUseCase
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
val useCasesModule =
    module {

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

        // Auth Use Cases
        factoryOf(::RequestSmsCodeUseCase)
        factoryOf(::VerifySmsCodeUseCase)
        factoryOf(::SaveAuthSessionUseCase)
        factoryOf(::GetAuthSessionUseCase)
        factoryOf(::HasAuthSessionUseCase)
        factoryOf(::ClearAuthSessionUseCase)

        // Load Use Cases
        factoryOf(::GetLoadsUseCase)
        factoryOf(::GetCachedLoadsUseCase)
        factoryOf(::ConnectToLoadUseCase)
        factoryOf(::DisconnectFromLoadUseCase)
    }
