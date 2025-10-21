package com.shiplocate.presentation.di

import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.HomeViewModel
import com.shiplocate.presentation.feature.loads.LoadsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация для инъекции ViewModels через Koin
 */
actual fun koinHomeViewModel(): HomeViewModel {
    return object : KoinComponent {
        val viewModel: HomeViewModel by inject()
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
