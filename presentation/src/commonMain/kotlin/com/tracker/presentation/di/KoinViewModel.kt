package com.tracker.presentation.di

import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.auth.EnterPhoneViewModel
import com.tracker.presentation.feature.auth.EnterPinViewModel
import com.tracker.presentation.feature.home.HomeViewModel

/**
 * Expect/Actual для инъекции ViewModels через Koin
 */
@Composable
expect fun koinHomeViewModel(): HomeViewModel

@Composable
expect fun koinEnterPhoneViewModel(): EnterPhoneViewModel

@Composable
expect fun koinEnterPinViewModel(): EnterPinViewModel
