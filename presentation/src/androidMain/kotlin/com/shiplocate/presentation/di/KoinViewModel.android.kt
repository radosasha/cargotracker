package com.shiplocate.presentation.di

import androidx.compose.runtime.Composable
import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.HomeViewModel
import com.shiplocate.presentation.feature.loads.LoadsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Android реализация для инъекции ViewModels через Koin
 */
@Composable
actual fun koinHomeViewModel(): HomeViewModel = koinViewModel()

@Composable
actual fun koinEnterPhoneViewModel(): EnterPhoneViewModel = koinViewModel()

@Composable
actual fun koinEnterPinViewModel(): EnterPinViewModel = koinViewModel()

@Composable
actual fun koinLoadsViewModel(): LoadsViewModel = koinViewModel()
