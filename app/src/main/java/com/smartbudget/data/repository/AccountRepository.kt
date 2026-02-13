package com.smartbudget.data.repository

import com.smartbudget.data.dao.AccountDao
import com.smartbudget.data.entity.Account
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun getDefaultAccount(): Account? = accountDao.getDefaultAccount()

    suspend fun getAccountById(id: Long): Account? = accountDao.getAccountById(id)

    suspend fun getAccountCount(): Int = accountDao.getAccountCount()

    suspend fun insert(account: Account): Long = accountDao.insert(account)

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(account: Account) = accountDao.delete(account)
}
