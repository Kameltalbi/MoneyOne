package com.smartbudget.data.dao

import androidx.room.*
import com.smartbudget.data.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth AND isGlobal = 1 LIMIT 1")
    fun getGlobalBudget(yearMonth: String): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth AND categoryId = :categoryId LIMIT 1")
    fun getCategoryBudget(yearMonth: String, categoryId: Long): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth")
    fun getAllBudgetsForMonth(yearMonth: String): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("DELETE FROM budgets WHERE yearMonth = :yearMonth AND isGlobal = 1")
    suspend fun deleteGlobalBudget(yearMonth: String)
}
