package com.shiplocate.domain.model.notification

/**
 * Numeric codes used in push notification payloads.
 * Keep in sync with server-side LoadPushNotificationService.
 */
object NotificationType {
    const val NEW_LOAD = 0
    const val LOAD_ASSIGNED = 1
    const val LOAD_UPDATED = 2
    const val STOP_ENTERED = 3
    const val LOAD_UNAVAILABLE = 4
    const val SILENT = 5
    const val DISPATCH_MESSAGE = 6
}

