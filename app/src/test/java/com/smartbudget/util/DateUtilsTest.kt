package com.smartbudget.util

import com.smartbudget.ui.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DateUtilsTest {

    @Test
    fun `toEpochMillis and fromEpochMillis are inverse`() {
        val date = LocalDate.of(2026, 2, 13)
        val millis = DateUtils.toEpochMillis(date)
        val result = DateUtils.fromEpochMillis(millis)
        assertEquals(date, result)
    }

    @Test
    fun `monthStart returns first day of month at midnight`() {
        val ym = YearMonth.of(2026, 2)
        val start = DateUtils.monthStart(ym)
        val date = DateUtils.fromEpochMillis(start)
        assertEquals(LocalDate.of(2026, 2, 1), date)
    }

    @Test
    fun `monthEnd returns first day of next month`() {
        val ym = YearMonth.of(2026, 2)
        val end = DateUtils.monthEnd(ym)
        val date = DateUtils.fromEpochMillis(end)
        assertEquals(LocalDate.of(2026, 3, 1), date)
    }

    @Test
    fun `dayStart returns start of day`() {
        val date = LocalDate.of(2026, 6, 15)
        val start = DateUtils.dayStart(date)
        assertEquals(DateUtils.toEpochMillis(date), start)
    }

    @Test
    fun `dayEnd returns start of next day`() {
        val date = LocalDate.of(2026, 6, 15)
        val end = DateUtils.dayEnd(date)
        val nextDay = DateUtils.toEpochMillis(date.plusDays(1))
        assertEquals(nextDay, end)
    }

    @Test
    fun `yearMonthString formats correctly`() {
        val ym = YearMonth.of(2026, 2)
        assertEquals("2026-02", DateUtils.yearMonthString(ym))
    }

    @Test
    fun `yearMonthString with december`() {
        val ym = YearMonth.of(2025, 12)
        assertEquals("2025-12", DateUtils.yearMonthString(ym))
    }

    @Test
    fun `toUtcMillis and fromUtcMillis are inverse`() {
        val date = LocalDate.of(2026, 7, 4)
        val millis = DateUtils.toUtcMillis(date)
        val result = DateUtils.fromUtcMillis(millis)
        assertEquals(date, result)
    }

    @Test
    fun `monthStart is before monthEnd`() {
        val ym = YearMonth.of(2026, 1)
        assertTrue(DateUtils.monthStart(ym) < DateUtils.monthEnd(ym))
    }

    @Test
    fun `dayStart is before dayEnd`() {
        val date = LocalDate.of(2026, 3, 20)
        assertTrue(DateUtils.dayStart(date) < DateUtils.dayEnd(date))
    }
}
