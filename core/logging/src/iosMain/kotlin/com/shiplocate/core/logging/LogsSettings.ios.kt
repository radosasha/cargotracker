package com.shiplocate.core.logging

actual class LogsSettings {
    actual fun getLogsDirectory(): String {
        return "/tmp/logs"
    }
}
