package com.smartbudget.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,       // Material icon name
    val color: Long,        // Color as ARGB long
    val type: TransactionType,
    val isDefault: Boolean = false,
    val userId: String = ""
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}
