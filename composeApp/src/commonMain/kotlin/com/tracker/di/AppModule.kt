package com.tracker.di

import com.tracker.data.di.dataModule
import com.tracker.domain.di.domainModule
import com.tracker.presentation.di.presentationModule

/**
 * Главный модуль приложения, объединяющий все слои
 */
val appModule = listOf(
    domainModule,
    dataModule,
    presentationModule
)