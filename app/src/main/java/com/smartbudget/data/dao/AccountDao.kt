package com.smartbudget.data.dao

import androidx.room.*
import com.smartbudget.data.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY isDefault DESC, name ASC")
    fun getAllAccounts(userId: String): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(userId: String): Account?

    @Query("SELECT * FROM accounts WHERE id = :id AND userId = :userId")
    suspend fun getAccountById(id: Long, userId: String): Account?

    @Query("SELECT COUNT(*) FROM accounts WHERE userId = :userId")
    suspend fun getAccountCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)
}
