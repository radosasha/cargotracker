package com.tracker.di

import com.tracker.data.di.dataModule
import com.tracker.presentation.di.presentationModule

/**
 * Главный модуль приложения, объединяющий все слои
 * 
 * Структура:
 * - dataModule: Data слой (Repositories, DataSources, Services)
 * - presentationModule: Presentation слой (Use Cases + ViewModels)
 *   - useCasesModule: Domain Use Cases
 *   - viewModelModule: Presentation ViewModels
 */
val appModule = dataModule + presentationModule
