package com.shiplocate.presentation.di

import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.LoadViewModel
import com.shiplocate.presentation.feature.loads.LoadsViewModel
import com.shiplocate.presentation.feature.logs.LogsViewModel
import com.shiplocate.presentation.feature.messages.MessagesViewModel
import com.shiplocate.presentation.feature.permissions.PermissionsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Модуль для регистрации ViewModels (Presentation слой)
 *
 * Factory scope - каждый ViewModel создается заново при каждом использовании
 */
val viewModelModule =
    module {

        // Feature ViewModels
        factoryOf(::LoadViewModel)
        factoryOf(::LoadsViewModel)
        factoryOf(::LogsViewModel)
        factoryOf(::PermissionsViewModel)
        factoryOf(::MessagesViewModel)

        // Auth ViewModels
        factoryOf(::EnterPhoneViewModel)
        factoryOf(::EnterPinViewModel)
    }
