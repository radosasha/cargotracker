package com.shiplocate.presentation.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility for formatting timestamps for UI display
 * Part of Presentation layer - handles UI-specific formatting
 */
object DateFormatter {
    /**
     * Format timestamp to localized date string with month name and time
     * Format: "DD MonthName YYYY HH:MM" (e.g., "13 October 2025 14:30")
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string with localized month name and time
     */
    fun formatDateWithMonthName(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthName = getLocalizedMonthName(localDateTime.monthNumber)
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        return "${localDateTime.dayOfMonth} $monthName ${localDateTime.year} $hour:$minute"
    }

    /**
     * Get localized month name in English
     *
     * @param monthNumber Month number (1-12)
     * @return Localized month name
     */
    private fun getLocalizedMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }
    }

    /**
     * Format timestamp to readable date and time string
     * Format: "YYYY-MM-DD HH:MM"
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date and time string
     */
    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.date} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    }

    /**
     * Format timestamp to date only
     * Format: "YYYY-MM-DD"
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string
     */
    fun formatDate(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return dateTime.date.toString()
    }

    /**
     * Format timestamp to time only
     * Format: "HH:MM"
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted time string
     */
    fun formatTime(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    }

    /**
     * Format timestamp to detailed date and time
     * Format: "YYYY-MM-DD HH:MM:SS"
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date and time string with seconds
     */
    fun formatTimestampDetailed(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.date} ${dateTime.hour.toString().padStart(
            2,
            '0',
        )}:${dateTime.minute.toString().padStart(2, '0')}:${dateTime.second.toString().padStart(2, '0')}"
    }

    /**
     * Format timestamp for message display
     * Format: "Nov 20, 2024 - 6:47 PM"
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date and time string
     */
    fun formatMessageDateTime(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthName = getShortMonthName(localDateTime.monthNumber)
        val day = localDateTime.dayOfMonth
        val year = localDateTime.year

        // Format time in 12-hour format with AM/PM
        val hour12 = if (localDateTime.hour == 0) {
            12
        } else if (localDateTime.hour > 12) {
            localDateTime.hour - 12
        } else {
            localDateTime.hour
        }
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val amPm = if (localDateTime.hour < 12) "AM" else "PM"

        return "$monthName $day, $year - $hour12:$minute $amPm"
    }

    /**
     * Get short month name
     *
     * @param monthNumber Month number (1-12)
     * @return Short month name (e.g., "Nov")
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
            else -> ""
        }
    }
}
