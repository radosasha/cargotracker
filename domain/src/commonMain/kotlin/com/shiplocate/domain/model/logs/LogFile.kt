package com.shiplocate.domain.model.logs

/**
 * Модель файла лога
 */
data class LogFile(
    val name: String,
    val size: Long,
    val isSelected: Boolean = false,
) {
    /**
     * Форматированный размер файла
     */
    val formattedSize: String
        get() = formatFileSize(size)

    /**
     * Форматирует размер файла в читаемый вид
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
