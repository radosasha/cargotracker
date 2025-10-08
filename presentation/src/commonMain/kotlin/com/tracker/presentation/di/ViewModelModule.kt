package com.tracker.presentation.di

import com.tracker.presentation.feature.home.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Модуль для регистрации ViewModels (Presentation слой)
 * 
 * Factory scope - каждый ViewModel создается заново при каждом использовании
 */
val viewModelModule = module {
    
    // Feature ViewModels
    factoryOf(::HomeViewModel)
}
