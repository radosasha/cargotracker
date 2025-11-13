package com.shiplocate.di

import androidx.compose.runtime.Composable
import com.shiplocate.presentation.feature.home.LoadViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
actual fun koinInjectLoadViewModel(): LoadViewModel {
    return object : KoinComponent {
        val viewModel: LoadViewModel by inject()
    }.viewModel
}
