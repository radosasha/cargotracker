package com.tracker.presentation.di

import com.tracker.presentation.feature.auth.EnterPhoneViewModel
import com.tracker.presentation.feature.auth.EnterPinViewModel
import com.tracker.presentation.feature.home.HomeViewModel
import com.tracker.presentation.feature.loads.LoadsViewModel
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
    factoryOf(::LoadsViewModel)
    
    // Auth ViewModels
    factoryOf(::EnterPhoneViewModel)
    factoryOf(::EnterPinViewModel)
}
