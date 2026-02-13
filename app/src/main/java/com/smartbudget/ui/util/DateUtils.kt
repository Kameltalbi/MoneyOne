package com.smartbudget.ui.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val zone = ZoneId.systemDefault()

    fun toEpochMillis(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun fromEpochMillis(millis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun monthStart(yearMonth: YearMonth): Long =
        toEpochMillis(yearMonth.atDay(1))

    fun monthEnd(yearMonth: YearMonth): Long =
        toEpochMillis(yearMonth.plusMonths(1).atDay(1))

    fun dayStart(date: LocalDate): Long = toEpochMillis(date)

    fun dayEnd(date: LocalDate): Long = toEpochMillis(date.plusDays(1))

    fun yearMonthString(yearMonth: YearMonth): String =
        yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    fun formatMonthYear(yearMonth: YearMonth): String {
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { it.uppercase() }
        return "$month ${yearMonth.year}"
    }

    fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))

    // UTC conversions for Material 3 DatePicker
    fun toUtcMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromUtcMillis(millis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}
