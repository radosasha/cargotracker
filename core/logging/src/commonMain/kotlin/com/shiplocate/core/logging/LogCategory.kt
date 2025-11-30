package com.shiplocate.core.logging

/**
 * Категории логирования для группировки логов по функциональности
 */
enum class LogCategory(val displayName: String) {
    GENERAL("GENERAL"),
    NETWORK("NETWORK"),
    DATABASE("DATABASE"),
    AUTH("AUTH"),
    LOCATION("LOCATION"),
    NOTIFICATIONS("NOTIFICATIONS"),
    UI("UI"),
    ERROR("ERROR"),
    PERMISSIONS("PERMISSIONS"),
    ;

    companion object {
        /**
         * Получить категорию по строке
         */
        fun fromString(category: String): LogCategory? {
            return values().find { it.name.equals(category, ignoreCase = true) }
        }
    }
}
