package com.smartbudget.data.repository

import com.smartbudget.data.dao.BudgetDao
import com.smartbudget.data.entity.Budget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getGlobalBudget(yearMonth: String): Flow<Budget?> =
        budgetDao.getGlobalBudget(yearMonth)

    fun getCategoryBudget(yearMonth: String, categoryId: Long): Flow<Budget?> =
        budgetDao.getCategoryBudget(yearMonth, categoryId)

    fun getAllBudgetsForMonth(yearMonth: String): Flow<List<Budget>> =
        budgetDao.getAllBudgetsForMonth(yearMonth)

    suspend fun insert(budget: Budget): Long = budgetDao.insert(budget)

    suspend fun update(budget: Budget) = budgetDao.update(budget)

    suspend fun delete(budget: Budget) = budgetDao.delete(budget)

    suspend fun deleteGlobalBudget(yearMonth: String) =
        budgetDao.deleteGlobalBudget(yearMonth)

    suspend fun getAllBudgetsForMonthDirect(yearMonth: String): List<Budget> =
        budgetDao.getAllBudgetsForMonthDirect(yearMonth)
}
