package com.shiplocate.presentation.model

/**
 * Состояние нижней навигационной панели (bottomBar) в Scaffold
 * Используется для типобезопасного управления состоянием bottomBar
 * в зависимости от текущего экрана
 */
sealed interface BottomBarState {
    /**
     * Состояние для экрана Loads с пагинацией Active/Upcoming
     */
    data class Loads(
        val currentPage: Int,
        val onPageSelected: (Int) -> Unit,
    ) : BottomBarState

    /**
     * Отсутствие bottomBar (для большинства экранов)
     */
    data object None : BottomBarState
}

