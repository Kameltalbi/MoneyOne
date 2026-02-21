package com.smartbudget.data.repository

import com.smartbudget.data.dao.RecurringDao
import com.smartbudget.data.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow

class RecurringRepository(private val recurringDao: RecurringDao) {

    suspend fun insert(recurring: RecurringTransaction): Long =
        recurringDao.insert(recurring)

    suspend fun update(recurring: RecurringTransaction) =
        recurringDao.update(recurring)

    suspend fun getActiveRecurring(userId: String): List<RecurringTransaction> =
        recurringDao.getActiveRecurring(userId)

    fun getActiveRecurringFlow(userId: String): Flow<List<RecurringTransaction>> =
        recurringDao.getActiveRecurringFlow(userId)

    suspend fun getById(id: Long, userId: String): RecurringTransaction? =
        recurringDao.getById(id, userId)

    suspend fun getActiveByAccount(userId: String, accountId: Long): List<RecurringTransaction> =
        recurringDao.getActiveByAccount(userId, accountId)
}
