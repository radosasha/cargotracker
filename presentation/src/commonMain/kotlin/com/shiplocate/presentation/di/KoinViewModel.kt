package com.shiplocate.presentation.di

import androidx.compose.runtime.Composable
import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.HomeViewModel
import com.shiplocate.presentation.feature.loads.LoadsViewModel

/**
 * Expect/Actual для инъекции ViewModels через Koin
 */
@Composable
expect fun koinHomeViewModel(): HomeViewModel

@Composable
expect fun koinEnterPhoneViewModel(): EnterPhoneViewModel

@Composable
expect fun koinEnterPinViewModel(): EnterPinViewModel

@Composable
expect fun koinLoadsViewModel(): LoadsViewModel
