package com.smartbudget.data.repository

import com.smartbudget.data.dao.TransactionDao
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getTransactionsForPeriod(
        accountId: Long, startDate: Long, endDate: Long
    ): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsForPeriod(accountId, startDate, endDate)

    fun getTransactionsForDay(
        accountId: Long, startDate: Long, endDate: Long
    ): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsForDay(accountId, startDate, endDate)

    fun getTotalIncome(accountId: Long, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getTotalIncome(accountId, startDate, endDate)

    fun getTotalExpenses(accountId: Long, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getTotalExpenses(accountId, startDate, endDate)

    fun getDailyBalance(accountId: Long, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getDailyBalance(accountId, startDate, endDate)

    fun getExpensesByCategory(
        accountId: Long, categoryId: Long, startDate: Long, endDate: Long
    ): Flow<Double> =
        transactionDao.getExpensesByCategory(accountId, categoryId, startDate, endDate)

    // All-accounts consolidated queries
    fun getAllTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsForPeriod(startDate, endDate)

    fun getAllTransactionsForDay(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsForDay(startDate, endDate)

    fun getAllTotalIncome(startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getAllTotalIncome(startDate, endDate)

    fun getAllTotalExpenses(startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getAllTotalExpenses(startDate, endDate)

    suspend fun insert(transaction: Transaction): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)
}
