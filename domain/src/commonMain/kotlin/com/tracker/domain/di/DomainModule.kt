package com.tracker.domain.di

import com.tracker.domain.service.LocationProcessor
import com.tracker.domain.service.LocationSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.tracker.domain.usecase.GetPermissionStatusUseCase
import com.tracker.domain.usecase.GetTrackingStatusUseCase
import com.tracker.domain.usecase.ProcessLocationUseCase
import com.tracker.domain.usecase.RequestAllPermissionsUseCase
import com.tracker.domain.usecase.StartTrackingUseCase
import com.tracker.domain.usecase.StopTrackingUseCase
import com.tracker.domain.usecase.TestServerUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Domain модуль с Use Cases и сервисами
 */
val domainModule = module {
    
    // Services (Singleton - живут весь жизненный цикл приложения)
    single { LocationProcessor() }
    single { LocationSyncService(get(), get()) }
    
    // CoroutineScope для LocationSyncManager
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    
    // Use Cases (Factory - создаются для каждого ViewModel)
    factoryOf(::GetPermissionStatusUseCase)
    factoryOf(::GetTrackingStatusUseCase)
    factoryOf(::RequestAllPermissionsUseCase)
    factoryOf(::StartTrackingUseCase)
    factoryOf(::StopTrackingUseCase)
    factoryOf(::ProcessLocationUseCase)
    factoryOf(::TestServerUseCase)
}
