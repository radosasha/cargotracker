package com.shiplocate.presentation.di

import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.LoadViewModel
import com.shiplocate.presentation.feature.loads.LoadsViewModel
import com.shiplocate.presentation.feature.logs.LogsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация для инъекции ViewModels через Koin
 */
actual fun koinLoadViewModel(): LoadViewModel {
    return object : KoinComponent {
        val viewModel: LoadViewModel by inject()
    }.viewModel
}

actual fun koinEnterPhoneViewModel(): EnterPhoneViewModel {
    return object : KoinComponent {
        val viewModel: EnterPhoneViewModel by inject()
    }.viewModel
}

actual fun koinEnterPinViewModel(): EnterPinViewModel {
    return object : KoinComponent {
        val viewModel: EnterPinViewModel by inject()
    }.viewModel
}

actual fun koinLoadsViewModel(): LoadsViewModel {
    return object : KoinComponent {
        val viewModel: LoadsViewModel by inject()
    }.viewModel
}

actual fun koinLogsViewModel(): LogsViewModel {
    return object : KoinComponent {
        val viewModel: LogsViewModel by inject()
    }.viewModel
}
