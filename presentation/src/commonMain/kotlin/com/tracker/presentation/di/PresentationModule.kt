package com.tracker.presentation.di

import com.tracker.presentation.feature.home.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Presentation модуль с ViewModels
 */
val presentationModule = module {
    
    // ViewModels
    factoryOf(::HomeViewModel)
}
