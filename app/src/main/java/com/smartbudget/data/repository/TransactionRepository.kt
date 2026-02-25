package com.smartbudget.data.repository

import com.smartbudget.data.dao.TransactionDao
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getTransactionsForPeriod(
        userId: String, accountId: Long, startDate: Long, endDate: Long
    ): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsForPeriod(userId, accountId, startDate, endDate)

    fun getTransactionsForDay(
        userId: String, accountId: Long, startDate: Long, endDate: Long
    ): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsForDay(userId, accountId, startDate, endDate)

    fun getTotalIncome(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getTotalIncome(userId, accountId, startDate, endDate)

    fun getTotalExpenses(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getTotalExpenses(userId, accountId, startDate, endDate)

    fun getTotalIncome(userId: String, accountId: Long, startDate: Long?, endDate: Long): Flow<Double> =
        transactionDao.getTotalIncome(userId, accountId, startDate, endDate)

    fun getTotalExpenses(userId: String, accountId: Long, startDate: Long?, endDate: Long): Flow<Double> =
        transactionDao.getTotalExpenses(userId, accountId, startDate, endDate)

    fun getDailyBalance(userId: String, accountId: Long, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getDailyBalance(userId, accountId, startDate, endDate)

    fun getExpensesByCategory(
        userId: String, accountId: Long, categoryId: Long, startDate: Long, endDate: Long
    ): Flow<Double> =
        transactionDao.getExpensesByCategory(userId, accountId, categoryId, startDate, endDate)

    // All-accounts consolidated queries
    fun getAllTransactionsForPeriod(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsForPeriod(userId, startDate, endDate)

    fun getAllTransactionsForDay(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsForDay(userId, startDate, endDate)

    fun getAllTotalIncome(userId: String, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getAllTotalIncome(userId, startDate, endDate)

    fun getAllTotalExpenses(userId: String, startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getAllTotalExpenses(userId, startDate, endDate)

    fun getAllTotalIncome(userId: String, startDate: Long?, endDate: Long): Flow<Double> =
        transactionDao.getAllTotalIncome(userId, startDate, endDate)

    fun getAllTotalExpenses(userId: String, startDate: Long?, endDate: Long): Flow<Double> =
        transactionDao.getAllTotalExpenses(userId, startDate, endDate)

    suspend fun insert(transaction: Transaction): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)
    
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }

    suspend fun getTransactionsByDateRangeDirect(userId: String, startDate: Long, endDate: Long): List<Transaction> {
        return transactionDao.getTransactionsByDateRangeDirect(userId, startDate, endDate)
    }

    suspend fun getTransactionById(id: Long, userId: String): Transaction? =
        transactionDao.getTransactionById(id, userId)

    fun searchTransactions(userId: String, query: String): Flow<List<TransactionWithCategory>> =
        transactionDao.searchTransactions(userId, query)

    suspend fun getTotalExpensesDirect(userId: String, accountId: Long, startDate: Long, endDate: Long): Double =
        transactionDao.getTotalExpensesDirect(userId, accountId, startDate, endDate)

    suspend fun getExpensesByCategoryDirect(userId: String, accountId: Long, categoryId: Long, startDate: Long, endDate: Long): Double =
        transactionDao.getExpensesByCategoryDirect(userId, accountId, categoryId, startDate, endDate)

    suspend fun getTotalBalance(userId: String, accountId: Long): Double =
        transactionDao.getTotalBalance(userId, accountId)

    suspend fun getTotalBalanceAllAccounts(userId: String): Double =
        transactionDao.getTotalBalanceAllAccounts(userId)

    suspend fun insertAll(transactions: List<Transaction>) =
        transactionDao.insertAll(transactions)

    suspend fun getOccurrenceByRecurringAndDate(userId: String, recurringId: Long, date: Long): Transaction? =
        transactionDao.getOccurrenceByRecurringAndDate(userId, recurringId, date)

    suspend fun getLastOccurrenceDateForRecurring(userId: String, recurringId: Long): Long? =
        transactionDao.getLastOccurrenceDateForRecurring(userId, recurringId)

    suspend fun getFutureOccurrences(userId: String, recurringId: Long, fromDate: Long): List<Transaction> =
        transactionDao.getFutureOccurrences(userId, recurringId, fromDate)

    suspend fun softDelete(userId: String, id: Long) = transactionDao.softDelete(userId, id)
    
    suspend fun updateFutureUnmodifiedOccurrences(
        userId: String, recurringId: Long, fromDate: Long, name: String, amount: Double,
        type: TransactionType, categoryId: Long?, note: String
    ) = transactionDao.updateFutureUnmodifiedOccurrences(userId, recurringId, fromDate, name, amount, type, categoryId, note)

    suspend fun updateAllUnmodifiedOccurrences(
        userId: String, recurringId: Long, name: String, amount: Double,
        type: TransactionType, categoryId: Long?, note: String
    ) = transactionDao.updateAllUnmodifiedOccurrences(userId, recurringId, name, amount, type, categoryId, note)

    suspend fun getAllOccurrencesByRecurringId(userId: String, recurringId: Long): List<Transaction> =
        transactionDao.getAllOccurrencesByRecurringId(userId, recurringId)

    suspend fun softDeleteFutureOccurrences(userId: String, recurringId: Long, fromDate: Long) =
        transactionDao.softDeleteFutureOccurrences(userId, recurringId, fromDate)

    suspend fun softDeleteAllOccurrences(userId: String, recurringId: Long) =
        transactionDao.softDeleteAllOccurrences(userId, recurringId)
}
