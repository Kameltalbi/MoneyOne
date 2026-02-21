package com.smartbudget.data.dao

import androidx.room.*
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getAllCategories(userId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type ORDER BY name ASC")
    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id AND userId = :userId")
    suspend fun getCategoryById(id: Long, userId: String): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
    
    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getAllCategoriesDirect(userId: String): List<Category>

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    suspend fun count(userId: String): Int
}
