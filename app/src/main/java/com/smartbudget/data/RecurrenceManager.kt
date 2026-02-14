package com.smartbudget.data

import com.smartbudget.data.entity.Recurrence
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.repository.TransactionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class RecurrenceManager(private val transactionRepo: TransactionRepository) {

    /**
     * Generate missing recurring transactions up to the end of [targetMonth].
     * Called each time the user navigates to a month.
     * Respects recurrenceEndDate if set.
     */
    suspend fun generateUpToMonth(targetMonth: YearMonth) {
        val recurringTransactions = transactionRepo.getRecurringTransactions()
        val generateUntil = targetMonth.atEndOfMonth()

        // Group by recurrenceGroupId, pick the earliest transaction as template
        val templates = mutableMapOf<Long, Transaction>()
        for (txn in recurringTransactions) {
            if (txn.recurrence == Recurrence.NONE) continue
            val groupId = txn.recurrenceGroupId ?: txn.id
            val existing = templates[groupId]
            if (existing == null || txn.date < existing.date) {
                templates[groupId] = txn
            }
        }

        for ((groupId, template) in templates) {
            // Respect end date if set
            val endDate = template.recurrenceEndDate?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }

            // Find the last occurrence date using groupId
            val lastOccurrenceMillis = transactionRepo.getLastOccurrenceDateByGroupId(groupId)
                ?: template.date

            val lastDate = Instant.ofEpochMilli(lastOccurrenceMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            var nextDate = getNextDate(lastDate, template.recurrence)

            while (!nextDate.isAfter(generateUntil)) {
                // Stop if past end date
                if (endDate != null && nextDate.isAfter(endDate)) break

                val millis = nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Check if a transaction already exists for this group at this date
                val count = transactionRepo.countTransactionsForGroupAtDate(groupId, millis)
                if (count == 0) {
                    transactionRepo.insert(
                        Transaction(
                            name = template.name,
                            amount = template.amount,
                            type = template.type,
                            categoryId = template.categoryId,
                            accountId = template.accountId,
                            date = millis,
                            note = template.note,
                            isValidated = false,
                            recurrence = template.recurrence,
                            recurrenceEndDate = template.recurrenceEndDate,
                            recurrenceGroupId = groupId
                        )
                    )
                }
                nextDate = getNextDate(nextDate, template.recurrence)
            }
        }
    }

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
}
