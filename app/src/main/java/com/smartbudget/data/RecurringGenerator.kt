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

        // Extend generation window based on frequency
        // Daily/Weekly: generate 3 months ahead to ensure continuity
        // Monthly/Yearly: generate only target month
        val monthsAhead = when (recurring.frequency) {
            Frequency.DAILY -> 3
            Frequency.WEEKLY -> 3
            Frequency.MONTHLY -> 1
            Frequency.YEARLY -> 1
        }
        val generateUntil = targetMonth.plusMonths(monthsAhead.toLong()).atEndOfMonth()
        
        val startDate = Instant.ofEpochMilli(recurring.startDate)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        val endDate = recurring.endDate?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        android.util.Log.d("RecurringGen", "generateUpToMonth: recurringId=${recurring.id}, startDate=$startDate (${recurring.startDate}), targetMonth=$targetMonth")

        // Always start from the recurring rule's startDate
        // This ensures that when the startDate is updated (e.g., changing day of month),
        // all future occurrences use the new date pattern
        var nextDate = startDate
        
        // Find last generated occurrence to avoid regenerating existing ones
        val lastOccurrenceMillis = transactionRepo.getLastOccurrenceDateForRecurring(recurring.userId, recurring.id)
        android.util.Log.d("RecurringGen", "Last occurrence date: $lastOccurrenceMillis")
        if (lastOccurrenceMillis != null) {
            val lastDate = Instant.ofEpochMilli(lastOccurrenceMillis)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            
            android.util.Log.d("RecurringGen", "lastDate=$lastDate, will skip ahead from startDate=$startDate")
            // Skip ahead to after the last occurrence, but maintain the day pattern from startDate
            while (!nextDate.isAfter(lastDate)) {
                nextDate = getNextDate(nextDate, recurring.frequency, recurring.interval)
            }
            android.util.Log.d("RecurringGen", "After skipping, nextDate=$nextDate")
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
