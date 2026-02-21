package com.smartbudget.data

import com.smartbudget.data.entity.Frequency
import com.smartbudget.data.entity.RecurringTransaction
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.repository.TransactionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class RecurringGenerator(private val transactionRepo: TransactionRepository) {

    suspend fun generateUpToMonth(
        recurring: RecurringTransaction,
        targetMonth: YearMonth
    ) {
        if (!recurring.isActive) return

        val generateUntil = targetMonth.atEndOfMonth()
        val startDate = Instant.ofEpochMilli(recurring.startDate)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        val endDate = recurring.endDate?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        // Find last generated occurrence
        val lastOccurrenceMillis = transactionRepo.getLastOccurrenceDateForRecurring(recurring.userId, recurring.id)
        val lastDate = if (lastOccurrenceMillis != null) {
            Instant.ofEpochMilli(lastOccurrenceMillis)
                .atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            // No occurrences yet — start from startDate minus one interval so first nextDate = startDate
            null
        }

        var nextDate = if (lastDate != null) {
            getNextDate(lastDate, recurring.frequency, recurring.interval)
        } else {
            startDate
        }

        val toInsert = mutableListOf<Transaction>()

        while (!nextDate.isAfter(generateUntil)) {
            if (endDate != null && nextDate.isAfter(endDate)) break

            val occurrenceDate = nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // Check if occurrence already exists at this date (avoid duplicates)
            val existing = transactionRepo.getOccurrenceByRecurringAndDate(recurring.userId, recurring.id, occurrenceDate)
            if (existing == null) {
                val transaction = Transaction(
                    name = recurring.name,
                    amount = recurring.amount,
                    type = recurring.type,
                    categoryId = recurring.categoryId,
                    accountId = recurring.accountId,
                    date = occurrenceDate,
                    note = recurring.note,
                    isValidated = false,
                    recurringId = recurring.id,
                    userId = recurring.userId
                )
                toInsert.add(transaction)
            }
            nextDate = getNextDate(nextDate, recurring.frequency, recurring.interval)
        }

        if (toInsert.isNotEmpty()) {
            transactionRepo.insertAll(toInsert)
        }
    }

    private fun getNextDate(from: LocalDate, frequency: Frequency, interval: Int): LocalDate {
        return when (frequency) {
            Frequency.DAILY -> from.plusDays(interval.toLong())
            Frequency.WEEKLY -> from.plusWeeks(interval.toLong())
            Frequency.MONTHLY -> from.plusMonths(interval.toLong())
            Frequency.YEARLY -> from.plusYears(interval.toLong())
        }
    }
}
