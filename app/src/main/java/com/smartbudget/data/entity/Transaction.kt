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
        Index("date")
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
    val date: Long,         // epoch millis
    val note: String = "",
    val isValidated: Boolean = true,
    val recurrence: Recurrence = Recurrence.NONE,
    val recurrenceEndDate: Long? = null,  // null = indéfini
    val recurrenceGroupId: Long? = null   // links recurring occurrences together
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
