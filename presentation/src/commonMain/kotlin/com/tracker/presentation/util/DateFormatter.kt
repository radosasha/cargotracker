package com.tracker.presentation.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility for formatting timestamps for UI display
 * Part of Presentation layer - handles UI-specific formatting
 */
object DateFormatter {
    
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
        return "${dateTime.date} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}:${dateTime.second.toString().padStart(2, '0')}"
    }
}






