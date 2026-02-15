package com.smartbudget.data.repository

import com.smartbudget.data.dao.RecurringDao
import com.smartbudget.data.entity.RecurringTransaction
import kotlinx.coroutines.flow.Flow

class RecurringRepository(private val recurringDao: RecurringDao) {

    suspend fun insert(recurring: RecurringTransaction): Long =
        recurringDao.insert(recurring)

    suspend fun update(recurring: RecurringTransaction) =
        recurringDao.update(recurring)

    suspend fun getActiveRecurring(): List<RecurringTransaction> =
        recurringDao.getActiveRecurring()

    fun getActiveRecurringFlow(): Flow<List<RecurringTransaction>> =
        recurringDao.getActiveRecurringFlow()

    suspend fun getById(id: Long): RecurringTransaction? =
        recurringDao.getById(id)

    suspend fun getActiveByAccount(accountId: Long): List<RecurringTransaction> =
        recurringDao.getActiveByAccount(accountId)
}
