package com.shiplocate.trackingsdk.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Таймер на корутинах для ParkingTracker
 * Автоматически запускается при создании и работает каждые 10 минут
 */
class ParkingTimeoutTimer(
    val timeoutMs: Long,
    private val scope: CoroutineScope
) {

    private var timerJob: Job? = null
    private val _timerEvent = MutableSharedFlow<Unit>(replay = 0)
    val timerEvent: SharedFlow<Unit> = _timerEvent.asSharedFlow()

    private var isRunning = false

    /**
     * Запускает таймер на указанное время
     */
    fun start(delayMs: Long) {
        if (isRunning) return
        isRunning = true
        timerJob = scope.launch {
            delay(delayMs)
            isRunning = false
            _timerEvent.emit(Unit)
        }
    }

    /**
     * Останавливает таймер
     */
    fun stop() {
        if (!isRunning) return
        timerJob?.cancel()
        timerJob = null
        isRunning = false
    }

    /**
     * Проверяет, запущен ли таймер
     */
    fun isRunning(): Boolean {
        return isRunning
    }
}
