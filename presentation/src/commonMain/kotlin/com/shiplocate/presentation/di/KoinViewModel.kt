package com.shiplocate.presentation.di

import androidx.compose.runtime.Composable
import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.HomeViewModel
import com.shiplocate.presentation.feature.loads.LoadsViewModel

/**
 * Expect/Actual для инъекции ViewModels через Koin
 */
expect fun koinHomeViewModel(): HomeViewModel

expect fun koinEnterPhoneViewModel(): EnterPhoneViewModel

expect fun koinEnterPinViewModel(): EnterPinViewModel

expect fun koinLoadsViewModel(): LoadsViewModel
