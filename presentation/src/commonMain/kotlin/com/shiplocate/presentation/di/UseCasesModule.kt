package com.shiplocate.presentation.di

import com.shiplocate.domain.usecase.GetActiveLoadUseCase
import com.shiplocate.domain.usecase.GetDeviceInfoUseCase
import com.shiplocate.domain.usecase.GetPermissionStatusUseCase
import com.shiplocate.domain.usecase.HandleFirebaseTokenUseCase
import com.shiplocate.domain.usecase.HandlePushNotificationWhenAppKilledUseCase
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import com.shiplocate.domain.usecase.NotifyPermissionGrantedUseCase
import com.shiplocate.domain.usecase.ObservePermissionsUseCase
import com.shiplocate.domain.usecase.ObserveReceivedPushesUseCase
import com.shiplocate.domain.usecase.OpenAirplaneModeSettingsUseCase
import com.shiplocate.domain.usecase.RequestBackgroundLocationPermissionUseCase
import com.shiplocate.domain.usecase.RequestBatteryOptimizationDisableUseCase
import com.shiplocate.domain.usecase.RequestEnableHighAccuracyUseCase
import com.shiplocate.domain.usecase.RequestLocationPermissionUseCase
import com.shiplocate.domain.usecase.RequestNotificationPermissionUseCase
import com.shiplocate.domain.usecase.SavePhoneNumberUseCase
import com.shiplocate.domain.usecase.SendCachedTokenOnAuthUseCase
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.StopTrackingIfLoadUnlinkedUseCase
import com.shiplocate.domain.usecase.StopTrackingUseCase
import com.shiplocate.domain.usecase.TestServerUseCase
import com.shiplocate.domain.usecase.auth.ClearAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.GetAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.LogoutUseCase
import com.shiplocate.domain.usecase.auth.RequestSmsCodeUseCase
import com.shiplocate.domain.usecase.auth.SaveAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.VerifySmsCodeUseCase
import com.shiplocate.domain.usecase.load.ConnectToLoadUseCase
import com.shiplocate.domain.usecase.load.DisconnectFromLoadUseCase
import com.shiplocate.domain.usecase.load.GetCachedLoadsUseCase
import com.shiplocate.domain.usecase.load.GetConnectedLoadUseCase
import com.shiplocate.domain.usecase.load.GetLoadsUseCase
import com.shiplocate.domain.usecase.load.RejectLoadUseCase
import com.shiplocate.domain.usecase.load.UpdateStopCompletionUseCase
import com.shiplocate.domain.usecase.logs.GetLogsClientIdUseCase
import com.shiplocate.domain.usecase.logs.GetLogsUseCase
import com.shiplocate.domain.usecase.message.FetchMessagesUseCase
import com.shiplocate.domain.usecase.message.ObserveMessagesUseCase
import com.shiplocate.domain.usecase.message.SendMessageUseCase
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
        factoryOf(::RequestNotificationPermissionUseCase)
        factoryOf(::RequestLocationPermissionUseCase)
        factoryOf(::RequestBackgroundLocationPermissionUseCase)
        factoryOf(::RequestBatteryOptimizationDisableUseCase)
        factoryOf(::RequestEnableHighAccuracyUseCase)
        factoryOf(::OpenAirplaneModeSettingsUseCase)
        factoryOf(::ObservePermissionsUseCase)
        factoryOf(::ObserveReceivedPushesUseCase)
        factoryOf(::NotifyPermissionGrantedUseCase)

        // Tracking Use Cases
        factoryOf(::GetActiveLoadUseCase)
        factoryOf(::StartTrackingUseCase)
        factoryOf(::StopTrackingUseCase)

        // Server Use Cases
        factoryOf(::TestServerUseCase)

        // Auth Use Cases
        factoryOf(::RequestSmsCodeUseCase)
        factoryOf(::VerifySmsCodeUseCase)
        factoryOf(::SaveAuthSessionUseCase)
        factoryOf(::GetAuthSessionUseCase)
        factoryOf(::HasAuthSessionUseCase)
        factoryOf(::ClearAuthSessionUseCase)
        factoryOf(::LogoutUseCase)
        factoryOf(::SavePhoneNumberUseCase)

        // Load Use Cases
        factoryOf(::GetLoadsUseCase)
        factoryOf(::GetCachedLoadsUseCase)
        factoryOf(::ConnectToLoadUseCase)
        factoryOf(::DisconnectFromLoadUseCase)
        factoryOf(::RejectLoadUseCase)
        factoryOf(::GetConnectedLoadUseCase)
        factoryOf(::StopTrackingIfLoadUnlinkedUseCase)
        factoryOf(::UpdateStopCompletionUseCase)

        // Firebase Token Use Cases
        factoryOf(::HandleFirebaseTokenUseCase)
        factoryOf(::ManageFirebaseTokensUseCase)
        factoryOf(::SendCachedTokenOnAuthUseCase)
        factoryOf(::HandlePushNotificationWhenAppKilledUseCase)

        // Device Use Cases
        factoryOf(::GetDeviceInfoUseCase)

        // Logs Use Cases
        factoryOf(::GetLogsUseCase)
        factoryOf(::GetLogsClientIdUseCase)

        // Message Use Cases
        factoryOf(::ObserveMessagesUseCase)
        factoryOf(::FetchMessagesUseCase)
        factoryOf(::SendMessageUseCase)
    }
