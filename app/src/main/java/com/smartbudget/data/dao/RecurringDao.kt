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

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveRecurring(userId: String): List<RecurringTransaction>

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isActive = 1")
    fun getActiveRecurringFlow(userId: String): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id AND userId = :userId")
    suspend fun getById(id: Long, userId: String): RecurringTransaction?

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isActive = 1 AND accountId = :accountId")
    suspend fun getActiveByAccount(userId: String, accountId: Long): List<RecurringTransaction>
}
