package com.smartbudget.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

@Entity(
    tableName = "recurring_transactions",
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
        Index("accountId")
    ]
)
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long? = null,
    val accountId: Long,
    val note: String = "",
    val startDate: Long,        // epoch millis
    val frequency: Frequency,
    val interval: Int = 1,
    val endDate: Long? = null,  // epoch millis, null = indefinite
    val isActive: Boolean = true
)
