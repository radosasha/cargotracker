package com.shiplocate.di

import com.shiplocate.core.logging.di.LoggingModules
import com.shiplocate.data.di.dataModule
import com.shiplocate.presentation.di.presentationModule

/**
 * Главный модуль приложения, объединяющий все слои
 *
 * Структура:
 * - dataModule: Data слой (Repositories, DataSources, Services)
 * - presentationModule: Presentation слой (Use Cases + ViewModels)
 *   - useCasesModule: Domain Use Cases
 *   - viewModelModule: Presentation ViewModels
 * - LoggingModules.all: Модули логирования
 */
val appModule = dataModule + presentationModule + LoggingModules.all
