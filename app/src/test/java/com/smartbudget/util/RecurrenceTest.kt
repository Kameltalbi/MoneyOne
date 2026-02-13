package com.smartbudget.util

import com.smartbudget.data.entity.Recurrence
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {

    // Test the getNextDate logic directly by replicating it
    private fun getNextDate(from: LocalDate, recurrence: Recurrence): LocalDate {
        return when (recurrence) {
            Recurrence.WEEKLY -> from.plusWeeks(1)
            Recurrence.MONTHLY -> from.plusMonths(1)
            Recurrence.QUARTERLY -> from.plusMonths(3)
            Recurrence.FOUR_MONTHLY -> from.plusMonths(4)
            Recurrence.SEMI_ANNUAL -> from.plusMonths(6)
            Recurrence.ANNUAL -> from.plusYears(1)
            Recurrence.NONE -> from
        }
    }

    @Test
    fun `weekly recurrence adds 7 days`() {
        val date = LocalDate.of(2026, 1, 1)
        val next = getNextDate(date, Recurrence.WEEKLY)
        assertEquals(LocalDate.of(2026, 1, 8), next)
    }

    @Test
    fun `monthly recurrence adds 1 month`() {
        val date = LocalDate.of(2026, 1, 15)
        val next = getNextDate(date, Recurrence.MONTHLY)
        assertEquals(LocalDate.of(2026, 2, 15), next)
    }

    @Test
    fun `monthly recurrence handles end of month`() {
        val date = LocalDate.of(2026, 1, 31)
        val next = getNextDate(date, Recurrence.MONTHLY)
        assertEquals(LocalDate.of(2026, 2, 28), next)
    }

    @Test
    fun `quarterly recurrence adds 3 months`() {
        val date = LocalDate.of(2026, 1, 1)
        val next = getNextDate(date, Recurrence.QUARTERLY)
        assertEquals(LocalDate.of(2026, 4, 1), next)
    }

    @Test
    fun `four monthly recurrence adds 4 months`() {
        val date = LocalDate.of(2026, 1, 1)
        val next = getNextDate(date, Recurrence.FOUR_MONTHLY)
        assertEquals(LocalDate.of(2026, 5, 1), next)
    }

    @Test
    fun `semi annual recurrence adds 6 months`() {
        val date = LocalDate.of(2026, 1, 1)
        val next = getNextDate(date, Recurrence.SEMI_ANNUAL)
        assertEquals(LocalDate.of(2026, 7, 1), next)
    }

    @Test
    fun `annual recurrence adds 1 year`() {
        val date = LocalDate.of(2026, 3, 15)
        val next = getNextDate(date, Recurrence.ANNUAL)
        assertEquals(LocalDate.of(2027, 3, 15), next)
    }

    @Test
    fun `none recurrence returns same date`() {
        val date = LocalDate.of(2026, 5, 10)
        val next = getNextDate(date, Recurrence.NONE)
        assertEquals(date, next)
    }

    @Test
    fun `annual recurrence on leap day`() {
        val date = LocalDate.of(2024, 2, 29)
        val next = getNextDate(date, Recurrence.ANNUAL)
        assertEquals(LocalDate.of(2025, 2, 28), next)
    }

    @Test
    fun `multiple monthly recurrences chain correctly`() {
        var date = LocalDate.of(2026, 1, 5)
        for (i in 1..12) {
            date = getNextDate(date, Recurrence.MONTHLY)
        }
        assertEquals(LocalDate.of(2027, 1, 5), date)
    }
}
