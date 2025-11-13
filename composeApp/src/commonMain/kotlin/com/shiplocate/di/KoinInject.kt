package com.shiplocate.di

import androidx.compose.runtime.Composable
import com.shiplocate.presentation.feature.home.LoadViewModel

/**
 * Expect/Actual для инъекции зависимостей через Koin
 */
@Composable
expect fun koinInjectLoadViewModel(): LoadViewModel
