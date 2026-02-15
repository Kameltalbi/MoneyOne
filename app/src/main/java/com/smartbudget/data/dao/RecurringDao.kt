package com.smartbudget.data.dao

import androidx.room.*
import com.smartbudget.data.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {

    @Insert
    suspend fun insert(recurring: RecurringTransaction): Long

    @Update
    suspend fun update(recurring: RecurringTransaction)

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1")
    suspend fun getActiveRecurring(): List<RecurringTransaction>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1")
    fun getActiveRecurringFlow(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransaction?

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND accountId = :accountId")
    suspend fun getActiveByAccount(accountId: Long): List<RecurringTransaction>
}
