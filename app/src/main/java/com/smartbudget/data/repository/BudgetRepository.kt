package com.smartbudget.data.repository

import com.smartbudget.data.dao.BudgetDao
import com.smartbudget.data.entity.Budget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getGlobalBudget(userId: String, yearMonth: String): Flow<Budget?> =
        budgetDao.getGlobalBudget(userId, yearMonth)

    fun getCategoryBudget(userId: String, yearMonth: String, categoryId: Long): Flow<Budget?> =
        budgetDao.getCategoryBudget(userId, yearMonth, categoryId)

    fun getAllBudgetsForMonth(userId: String, yearMonth: String): Flow<List<Budget>> =
        budgetDao.getAllBudgetsForMonth(userId, yearMonth)

    suspend fun insert(budget: Budget): Long = budgetDao.insert(budget)

    suspend fun update(budget: Budget) = budgetDao.update(budget)

    suspend fun delete(budget: Budget) = budgetDao.delete(budget)

    suspend fun deleteGlobalBudget(userId: String, yearMonth: String) =
        budgetDao.deleteGlobalBudget(userId, yearMonth)

    suspend fun deleteCategoryBudget(userId: String, categoryId: Long) {
        budgetDao.deleteCategoryBudget(userId, categoryId)
    }
    
    suspend fun deleteBudget(budgetId: Long) {
        budgetDao.deleteBudget(budgetId)
    }

    suspend fun getAllBudgetsForMonthDirect(userId: String, yearMonth: String): List<Budget> =
        budgetDao.getAllBudgetsForMonthDirect(userId, yearMonth)
}
