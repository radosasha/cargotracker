package com.shiplocate.core.logging

/**
 * Уровни логирования от низкого к высокому приоритету
 */
enum class LogLevel(val priority: Int) {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    FATAL(5),
    ;

    /**
     * Проверяет, должен ли лог с данным уровнем быть записан
     */
    fun shouldLog(minLevel: LogLevel): Boolean {
        return this.priority >= minLevel.priority
    }
}
