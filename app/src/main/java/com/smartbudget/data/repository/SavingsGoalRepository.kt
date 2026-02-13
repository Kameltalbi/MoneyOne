package com.smartbudget.data.repository

import com.smartbudget.data.dao.SavingsGoalDao
import com.smartbudget.data.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

class SavingsGoalRepository(private val savingsGoalDao: SavingsGoalDao) {

    fun getAllGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()

    suspend fun getGoalById(id: Long): SavingsGoal? = savingsGoalDao.getGoalById(id)

    suspend fun insert(goal: SavingsGoal): Long = savingsGoalDao.insert(goal)

    suspend fun update(goal: SavingsGoal) = savingsGoalDao.update(goal)

    suspend fun delete(goal: SavingsGoal) = savingsGoalDao.delete(goal)
}
