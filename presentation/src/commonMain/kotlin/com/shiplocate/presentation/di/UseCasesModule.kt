package com.shiplocate.presentation.di

import com.shiplocate.domain.usecase.GetDeviceInfoUseCase
import com.shiplocate.domain.usecase.GetPermissionStatusUseCase
import com.shiplocate.domain.usecase.GetTrackingStatusUseCase
import com.shiplocate.domain.usecase.HandleFirebaseTokenUseCase
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import com.shiplocate.domain.usecase.RequestAllPermissionsUseCase
import com.shiplocate.domain.usecase.RequestNotificationPermissionUseCase
import com.shiplocate.domain.usecase.SendCachedTokenOnAuthUseCase
import com.shiplocate.domain.usecase.StartProcessLocationsUseCase
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.StopProcessLocationsUseCase
import com.shiplocate.domain.usecase.StopTrackingUseCase
import com.shiplocate.domain.usecase.TestServerUseCase
import com.shiplocate.domain.usecase.auth.ClearAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.GetAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.RequestSmsCodeUseCase
import com.shiplocate.domain.usecase.auth.SaveAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.VerifySmsCodeUseCase
import com.shiplocate.domain.usecase.SavePhoneNumberUseCase
import com.shiplocate.domain.usecase.load.ConnectToLoadUseCase
import com.shiplocate.domain.usecase.load.DisconnectFromLoadUseCase
import com.shiplocate.domain.usecase.load.GetCachedLoadsUseCase
import com.shiplocate.domain.usecase.load.GetLoadsUseCase
import com.shiplocate.domain.usecase.logs.GetLogsUseCase
import com.shiplocate.domain.usecase.logs.SendLogsUseCase
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
        factoryOf(::RequestNotificationPermissionUseCase)

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
        factoryOf(::SavePhoneNumberUseCase)

        // Load Use Cases
        factoryOf(::GetLoadsUseCase)
        factoryOf(::GetCachedLoadsUseCase)
        factoryOf(::ConnectToLoadUseCase)
        factoryOf(::DisconnectFromLoadUseCase)

        // Firebase Token Use Cases
        factoryOf(::HandleFirebaseTokenUseCase)
        factoryOf(::ManageFirebaseTokensUseCase)
        factoryOf(::SendCachedTokenOnAuthUseCase)

        // Device Use Cases
        factoryOf(::GetDeviceInfoUseCase)

        // Logs Use Cases
        factoryOf(::GetLogsUseCase)
        factoryOf(::SendLogsUseCase)
    }
