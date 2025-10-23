package com.shiplocate.domain.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Утилита для форматирования дат в удобочитаемый вид
 */
object DateFormatter {
    /**
     * Форматирует дату в формат "13 Mon, 14:20:10"
     * @param instant время для форматирования
     * @return отформатированная строка
     */
    fun formatForNotification(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = localDateTime.dayOfMonth
        val month = getShortMonthName(localDateTime.monthNumber)
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val second = localDateTime.second.toString().padStart(2, '0')

        return "$day $month, $hour:$minute:$second"
    }

    /**
     * Возвращает сокращенное название месяца
     */
    private fun getShortMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "???"
        }
    }
}
