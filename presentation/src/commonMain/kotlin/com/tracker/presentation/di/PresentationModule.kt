package com.tracker.presentation.di

/**
 * Presentation модуль - объединяет Use Cases и ViewModels
 *
 * Разделен на два подмодуля для лучшей организации:
 * - useCasesModule - Domain Use Cases (без DI зависимостей в domain)
 * - viewModelModule - Presentation ViewModels
 */
val presentationModule =
    listOf(
        useCasesModule,
        viewModelModule,
    )
