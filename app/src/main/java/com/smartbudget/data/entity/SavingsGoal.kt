package com.smartbudget.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val icon: String = "savings",
    val color: Long = 0xFF4CAF50,
    val createdDate: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,  // optional deadline
    val userId: String = ""
)
