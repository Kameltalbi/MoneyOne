package com.smartbudget.data.repository

import com.smartbudget.data.dao.CategoryDao
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun insertAll(categories: List<Category>) = categoryDao.insertAll(categories)

    suspend fun update(category: Category) = categoryDao.update(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)

    suspend fun count(): Int = categoryDao.count()
}
