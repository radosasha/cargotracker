package com.shiplocate.presentation.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface NavigationEvent {
    data class OpenMessages(val loadId: Long) : NavigationEvent
}

/**
 * Simple event bus for navigation actions triggered outside of Compose navigation scope (e.g. push taps).
 */
object NavigationEventBus {
    private val _events = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<NavigationEvent> = _events.asSharedFlow()

    fun publish(event: NavigationEvent) {
        _events.tryEmit(event)
    }
}

