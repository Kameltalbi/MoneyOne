package com.smartbudget.data.repository

import com.smartbudget.data.dao.AccountDao
import com.smartbudget.data.entity.Account
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {
    fun getAllAccounts(userId: String): Flow<List<Account>> = accountDao.getAllAccounts(userId)

    suspend fun getDefaultAccount(userId: String): Account? = accountDao.getDefaultAccount(userId)

    suspend fun getAccountById(id: Long, userId: String): Account? = accountDao.getAccountById(id, userId)

    suspend fun getAccountCount(userId: String): Int = accountDao.getAccountCount(userId)

    suspend fun insert(account: Account): Long = accountDao.insert(account)

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(account: Account) = accountDao.delete(account)
}
