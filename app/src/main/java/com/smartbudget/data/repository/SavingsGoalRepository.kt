package com.smartbudget.data.repository

import com.smartbudget.data.dao.SavingsGoalDao
import com.smartbudget.data.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

class SavingsGoalRepository(private val savingsGoalDao: SavingsGoalDao) {

    fun getAllGoals(userId: String): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals(userId)

    suspend fun getGoalById(id: Long, userId: String): SavingsGoal? = savingsGoalDao.getGoalById(id, userId)

    suspend fun insert(goal: SavingsGoal): Long = savingsGoalDao.insert(goal)

    suspend fun update(goal: SavingsGoal) = savingsGoalDao.update(goal)
    
    suspend fun deleteGoal(goal: SavingsGoal) = savingsGoalDao.delete(goal)

    suspend fun delete(goal: SavingsGoal) = savingsGoalDao.delete(goal)
}
