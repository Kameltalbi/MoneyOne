package com.smartbudget.data.dao

import androidx.room.*
import com.smartbudget.data.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY createdDate DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal): Long

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)
}
