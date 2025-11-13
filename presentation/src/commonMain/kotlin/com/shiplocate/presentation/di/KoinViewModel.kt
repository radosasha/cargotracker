package com.shiplocate.presentation.di

import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.LoadViewModel
import com.shiplocate.presentation.feature.loads.LoadsViewModel
import com.shiplocate.presentation.feature.logs.LogsViewModel
import com.shiplocate.presentation.feature.permissions.PermissionsViewModel

/**
 * Expect/Actual для инъекции ViewModels через Koin
 */
expect fun koinLoadViewModel(): LoadViewModel

expect fun koinEnterPhoneViewModel(): EnterPhoneViewModel

expect fun koinEnterPinViewModel(): EnterPinViewModel

expect fun koinLoadsViewModel(): LoadsViewModel

expect fun koinLogsViewModel(): LogsViewModel

expect fun koinPermissionsViewModel(): PermissionsViewModel
