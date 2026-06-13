package com.smartbudget.lib

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Utility object for date manipulation and formatting.
 * Provides convenient methods for converting between LocalDate and epoch milliseconds,
 * formatting dates, and working with month boundaries.
 */
object DateUtils {
    private val zone = ZoneId.systemDefault()

    /**
     * Converts a LocalDate to epoch milliseconds at the start of the day in the system timezone.
     */
    fun toEpochMillis(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    /**
     * Converts epoch milliseconds to a LocalDate in the system timezone.
     */
    fun fromEpochMillis(millis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    /**
     * Returns the epoch milliseconds for the start of the given month.
     */
    fun monthStart(yearMonth: YearMonth): Long =
        toEpochMillis(yearMonth.atDay(1))

    /**
     * Returns the epoch milliseconds for the end of the given month (start of next month).
     */
    fun monthEnd(yearMonth: YearMonth): Long =
        toEpochMillis(yearMonth.plusMonths(1).atDay(1))

    /**
     * Returns the epoch milliseconds for the start of the given day.
     */
    fun dayStart(date: LocalDate): Long = toEpochMillis(date)

    /**
     * Returns the epoch milliseconds for the end of the given day (start of next day).
     */
    fun dayEnd(date: LocalDate): Long = toEpochMillis(date.plusDays(1))

    /**
     * Formats a YearMonth as "yyyy-MM" string.
     */
    fun yearMonthString(yearMonth: YearMonth): String =
        yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    /**
     * Formats a YearMonth as "Month Year" (e.g., "January 2024").
     */
    fun formatMonthYear(yearMonth: YearMonth): String {
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { it.uppercase() }
        return "$month ${yearMonth.year}"
    }

    /**
     * Formats a LocalDate as "dd MMMM yyyy" (e.g., "15 January 2024").
     */
    fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))

    /**
     * Formats a LocalDate as "dd MMMM" without year (e.g., "15 January").
     */
    fun formatDateDayOnly(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault()))

    /**
     * Converts a LocalDate to UTC epoch milliseconds (for Material 3 DatePicker compatibility).
     */
    fun toUtcMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /**
     * Converts UTC epoch milliseconds to a LocalDate (for Material 3 DatePicker compatibility).
     */
    fun fromUtcMillis(millis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}
