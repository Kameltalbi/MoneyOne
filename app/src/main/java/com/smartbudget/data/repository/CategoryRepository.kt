package com.smartbudget.data.repository

import com.smartbudget.data.dao.CategoryDao
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAllCategories(userId: String): Flow<List<Category>> = categoryDao.getAllCategories(userId)

    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(userId, type)

    suspend fun getCategoryById(id: Long, userId: String): Category? = categoryDao.getCategoryById(id, userId)

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun insertAll(categories: List<Category>) = categoryDao.insertAll(categories)

    suspend fun update(category: Category) = categoryDao.update(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)
    
    suspend fun getAllCategoriesDirect(userId: String): List<Category> {
        return categoryDao.getAllCategoriesDirect(userId)
    }

    suspend fun count(userId: String): Int = categoryDao.count(userId)
}
