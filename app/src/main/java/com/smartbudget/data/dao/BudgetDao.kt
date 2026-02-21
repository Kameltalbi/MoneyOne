package com.smartbudget.data.dao

import androidx.room.*
import com.smartbudget.data.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId AND yearMonth = :yearMonth AND isGlobal = 1 LIMIT 1")
    fun getGlobalBudget(userId: String, yearMonth: String): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND yearMonth = :yearMonth AND categoryId = :categoryId LIMIT 1")
    fun getCategoryBudget(userId: String, yearMonth: String, categoryId: Long): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND yearMonth = :yearMonth")
    fun getAllBudgetsForMonth(userId: String, yearMonth: String): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("DELETE FROM budgets WHERE userId = :userId AND yearMonth = :yearMonth AND isGlobal = 1")
    suspend fun deleteGlobalBudget(userId: String, yearMonth: String)

    @Query("DELETE FROM budgets WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun deleteCategoryBudget(userId: String, categoryId: Long)
    
    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: Long)

    @Query("SELECT * FROM budgets WHERE userId = :userId AND yearMonth = :yearMonth")
    suspend fun getAllBudgetsForMonthDirect(userId: String, yearMonth: String): List<Budget>
}
