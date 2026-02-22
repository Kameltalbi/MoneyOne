package com.smartbudget.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("accountId"),
        Index("date"),
        Index("recurringId")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long? = null,
    val accountId: Long,
    val destinationAccountId: Long? = null,  // For TRANSFER type: destination account
    val date: Long,         // epoch millis
    val note: String = "",
    val isValidated: Boolean = true,
    val recurringId: Long? = null,      // FK to recurring_transactions
    val isDeleted: Boolean = false,     // soft delete for recurring exceptions
    val isModified: Boolean = false,    // tracks if occurrence was individually modified
    val userId: String = "",
    // Legacy columns kept for DB compatibility
    val recurrence: Recurrence = Recurrence.NONE,
    val recurrenceEndDate: Long? = null,
    val recurrenceGroupId: Long? = null
)

enum class Recurrence {
    NONE,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    FOUR_MONTHLY,
    SEMI_ANNUAL,
    ANNUAL
}
