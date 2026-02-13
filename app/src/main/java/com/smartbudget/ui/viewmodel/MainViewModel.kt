package com.smartbudget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class MonthSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val budgetAmount: Double = 0.0,
    val budgetDifference: Double = 0.0,
    val budgetUsedPercent: Double = 0.0,
    val hasBudget: Boolean = false
)

data class DayData(
    val date: LocalDate,
    val balance: Double = 0.0,
    val hasTransactions: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SmartBudgetApp
    private val accountRepo = app.accountRepository
    private val transactionRepo = app.transactionRepository
    private val budgetRepo = app.budgetRepository

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentAccount = MutableStateFlow<Account?>(null)
    val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    private val _isConsolidated = MutableStateFlow(false)
    val isConsolidated: StateFlow<Boolean> = _isConsolidated.asStateFlow()

    val accounts: StateFlow<List<Account>> = accountRepo.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val defaultAccount = accountRepo.getDefaultAccount()
            _currentAccount.value = defaultAccount
        }
    }

    fun selectAccount(account: Account) {
        _currentAccount.value = account
        _isConsolidated.value = false
    }

    fun selectAllAccounts() {
        _isConsolidated.value = true
    }

    fun navigateMonth(offset: Int) {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(offset.toLong())
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // Monthly transactions
    val monthlyTransactions: StateFlow<List<TransactionWithCategory>> =
        combine(_currentAccount, _currentYearMonth, _isConsolidated) { account, yearMonth, consolidated ->
            Triple(account, yearMonth, consolidated)
        }.flatMapLatest { (account, yearMonth, consolidated) ->
            val start = DateUtils.monthStart(yearMonth)
            val end = DateUtils.monthEnd(yearMonth)
            if (consolidated) {
                transactionRepo.getAllTransactionsForPeriod(start, end)
            } else if (account == null) {
                flowOf(emptyList())
            } else {
                transactionRepo.getTransactionsForPeriod(account.id, start, end)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily transactions for selected date
    val dailyTransactions: StateFlow<List<TransactionWithCategory>> =
        combine(_currentAccount, _selectedDate, _isConsolidated) { account, date, consolidated ->
            Triple(account, date, consolidated)
        }.flatMapLatest { (account, date, consolidated) ->
            val start = DateUtils.dayStart(date)
            val end = DateUtils.dayEnd(date)
            if (consolidated) {
                transactionRepo.getAllTransactionsForDay(start, end)
            } else if (account == null) {
                flowOf(emptyList())
            } else {
                transactionRepo.getTransactionsForDay(account.id, start, end)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month summary
    val monthSummary: StateFlow<MonthSummary> =
        combine(_currentAccount, _currentYearMonth, _isConsolidated) { account, yearMonth, consolidated ->
            Triple(account, yearMonth, consolidated)
        }.flatMapLatest { (account, yearMonth, consolidated) ->
            val start = DateUtils.monthStart(yearMonth)
            val end = DateUtils.monthEnd(yearMonth)
            val ymStr = DateUtils.yearMonthString(yearMonth)

            if (consolidated) {
                combine(
                    transactionRepo.getAllTotalIncome(start, end),
                    transactionRepo.getAllTotalExpenses(start, end),
                    budgetRepo.getGlobalBudget(ymStr)
                ) { income, expenses, budget ->
                    val balance = income - expenses
                    val budgetAmt = budget?.amount ?: 0.0
                    val diff = budgetAmt - expenses
                    val pct = if (budgetAmt > 0) (expenses / budgetAmt) * 100 else 0.0
                    MonthSummary(
                        totalIncome = income,
                        totalExpenses = expenses,
                        balance = balance,
                        budgetAmount = budgetAmt,
                        budgetDifference = diff,
                        budgetUsedPercent = pct,
                        hasBudget = budget != null
                    )
                }
            } else if (account == null) {
                flowOf(MonthSummary())
            } else {
                combine(
                    transactionRepo.getTotalIncome(account.id, start, end),
                    transactionRepo.getTotalExpenses(account.id, start, end),
                    budgetRepo.getGlobalBudget(ymStr)
                ) { income, expenses, budget ->
                    val balance = income - expenses
                    val budgetAmt = budget?.amount ?: 0.0
                    val diff = budgetAmt - expenses
                    val pct = if (budgetAmt > 0) (expenses / budgetAmt) * 100 else 0.0
                    MonthSummary(
                        totalIncome = income,
                        totalExpenses = expenses,
                        balance = balance,
                        budgetAmount = budgetAmt,
                        budgetDifference = diff,
                        budgetUsedPercent = pct,
                        hasBudget = budget != null
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthSummary())

    // Balance up to selected date (not the whole month)
    val balanceUpToDate: StateFlow<Double> =
        combine(_currentAccount, _selectedDate, _isConsolidated) { account, date, consolidated ->
            Triple(account, date, consolidated)
        }.flatMapLatest { (account, date, consolidated) ->
            val start = DateUtils.monthStart(YearMonth.from(date))
            val end = DateUtils.dayEnd(date)
            if (consolidated) {
                combine(
                    transactionRepo.getAllTotalIncome(start, end),
                    transactionRepo.getAllTotalExpenses(start, end)
                ) { income, expenses -> income - expenses }
            } else if (account == null) {
                flowOf(0.0)
            } else {
                combine(
                    transactionRepo.getTotalIncome(account.id, start, end),
                    transactionRepo.getTotalExpenses(account.id, start, end)
                ) { income, expenses -> income - expenses }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category budgets for the month
    val categoryBudgets: StateFlow<List<Budget>> = _currentYearMonth.flatMapLatest { ym ->
        budgetRepo.getAllBudgetsForMonth(DateUtils.yearMonthString(ym))
    }.map { budgets ->
        budgets.filter { !it.isGlobal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Day balances for calendar display
    val dayBalances: StateFlow<Map<LocalDate, Double>> =
        monthlyTransactions.map { transactions ->
            val map = mutableMapOf<LocalDate, Double>()
            for (t in transactions) {
                val date = DateUtils.fromEpochMillis(t.date)
                val current = map.getOrDefault(date, 0.0)
                val delta = if (t.type == com.smartbudget.data.entity.TransactionType.INCOME) t.amount else -t.amount
                map[date] = current + delta
            }
            map.toMap()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun toggleTransactionValidation(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId) ?: return@launch
            transactionRepo.update(transaction.copy(isValidated = !transaction.isValidated))
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId) ?: return@launch
            transactionRepo.delete(transaction)
        }
    }

    fun duplicateTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId) ?: return@launch
            transactionRepo.insert(transaction.copy(id = 0))
        }
    }

    fun setInitialBalance(amount: Double) {
        if (amount == 0.0) return
        viewModelScope.launch {
            val account = accountRepo.getDefaultAccount() ?: return@launch
            val transaction = Transaction(
                name = "Solde initial",
                amount = amount,
                type = TransactionType.INCOME,
                accountId = account.id,
                date = System.currentTimeMillis(),
                note = "Solde initial du compte",
                isValidated = true
            )
            transactionRepo.insert(transaction)
        }
    }
}
