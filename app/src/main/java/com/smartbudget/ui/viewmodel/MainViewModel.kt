package com.smartbudget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.RecurringGenerator
import com.smartbudget.widget.BalanceWidgetProvider
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.entity.SavingsGoal
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

data class MonthlyTotal(
    val month: YearMonth,
    val income: Double,
    val expenses: Double
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

    private val recurringGenerator = RecurringGenerator(transactionRepo)
    private val recurringRepo = (application as SmartBudgetApp).recurringRepository

    init {
        // Generate recurring transactions for current month on startup
        viewModelScope.launch {
            generateRecurringForMonth(_currentYearMonth.value)
        }
    }

    fun navigateMonth(offset: Int) {
        val newMonth = _currentYearMonth.value.plusMonths(offset.toLong())
        _currentYearMonth.value = newMonth
        viewModelScope.launch {
            generateRecurringForMonth(newMonth)
        }
    }

    private suspend fun generateRecurringForMonth(month: YearMonth) {
        val activeRules = recurringRepo.getActiveRecurring()
        for (rule in activeRules) {
            recurringGenerator.generateUpToMonth(rule, month)
        }
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
            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun duplicateTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId) ?: return@launch
            transactionRepo.insert(transaction.copy(id = 0))
        }
    }

    fun duplicateTransactionToDate(transactionId: Long, dateMillis: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId) ?: return@launch
            transactionRepo.insert(transaction.copy(id = 0, date = dateMillis))
        }
    }

    fun createFirstAccount(name: String, initialBalance: Double) {
        viewModelScope.launch {
            val accountId = accountRepo.insert(Account(name = name, isDefault = true))
            _currentAccount.value = accountRepo.getDefaultAccount()
            if (initialBalance > 0) {
                val transaction = Transaction(
                    name = "Solde initial",
                    amount = initialBalance,
                    type = TransactionType.INCOME,
                    accountId = accountId,
                    date = System.currentTimeMillis(),
                    note = "Solde initial du compte",
                    isValidated = true
                )
                transactionRepo.insert(transaction)
            }
        }
    }

    // End of month forecast
    data class MonthForecast(
        val projectedExpenses: Double = 0.0,
        val projectedIncome: Double = 0.0,
        val projectedBalance: Double = 0.0,
        val daysElapsed: Int = 0,
        val daysInMonth: Int = 30,
        val dailyExpenseRate: Double = 0.0,
        val dailyIncomeRate: Double = 0.0
    )

    private val _monthForecast = MutableStateFlow(MonthForecast())
    val monthForecast: StateFlow<MonthForecast> = _monthForecast.asStateFlow()

    private fun loadMonthForecast() {
        viewModelScope.launch {
            val ym = _currentYearMonth.value
            val today = java.time.LocalDate.now()
            val daysInMonth = ym.lengthOfMonth()

            // Only forecast for current month
            if (ym != YearMonth.from(today)) {
                _monthForecast.value = MonthForecast()
                return@launch
            }

            val dayOfMonth = today.dayOfMonth
            if (dayOfMonth <= 1) {
                _monthForecast.value = MonthForecast()
                return@launch
            }

            val start = DateUtils.monthStart(ym)
            val nowEnd = DateUtils.dayEnd(today)
            val account = _currentAccount.value
            val consolidated = _isConsolidated.value

            val currentIncome: Double
            val currentExpenses: Double
            if (consolidated || account == null) {
                currentIncome = app.getDb().transactionDao().getAllTotalByType(TransactionType.INCOME, start, nowEnd)
                currentExpenses = app.getDb().transactionDao().getAllTotalByType(TransactionType.EXPENSE, start, nowEnd)
            } else {
                currentIncome = app.getDb().transactionDao().getTotalByType(account.id, TransactionType.INCOME, start, nowEnd)
                currentExpenses = app.getDb().transactionDao().getTotalByType(account.id, TransactionType.EXPENSE, start, nowEnd)
            }

            val dailyExpenseRate = currentExpenses / dayOfMonth
            val dailyIncomeRate = currentIncome / dayOfMonth
            val projectedExpenses = dailyExpenseRate * daysInMonth
            val projectedIncome = dailyIncomeRate * daysInMonth

            _monthForecast.value = MonthForecast(
                projectedExpenses = projectedExpenses,
                projectedIncome = projectedIncome,
                projectedBalance = projectedIncome - projectedExpenses,
                daysElapsed = dayOfMonth,
                daysInMonth = daysInMonth,
                dailyExpenseRate = dailyExpenseRate,
                dailyIncomeRate = dailyIncomeRate
            )
        }
    }

    // Previous month summary for comparison
    private val _previousMonthSummary = MutableStateFlow(MonthSummary())
    val previousMonthSummary: StateFlow<MonthSummary> = _previousMonthSummary.asStateFlow()

    private fun loadPreviousMonthSummary() {
        viewModelScope.launch {
            val ym = _currentYearMonth.value.minusMonths(1)
            val start = DateUtils.monthStart(ym)
            val end = DateUtils.monthEnd(ym)
            val account = _currentAccount.value
            val consolidated = _isConsolidated.value

            val income: Double
            val expenses: Double
            if (consolidated || account == null) {
                income = app.getDb().transactionDao().getAllTotalByType(TransactionType.INCOME, start, end)
                expenses = app.getDb().transactionDao().getAllTotalByType(TransactionType.EXPENSE, start, end)
            } else {
                income = app.getDb().transactionDao().getTotalByType(account.id, TransactionType.INCOME, start, end)
                expenses = app.getDb().transactionDao().getTotalByType(account.id, TransactionType.EXPENSE, start, end)
            }
            _previousMonthSummary.value = MonthSummary(
                totalIncome = income,
                totalExpenses = expenses,
                balance = income - expenses
            )
        }
    }

    init {
        // Reload previous month data and forecast when month or account changes
        viewModelScope.launch {
            combine(_currentYearMonth, _currentAccount, _isConsolidated) { _, _, _ -> Unit }
                .collect {
                    loadPreviousMonthSummary()
                    loadMonthForecast()
                }
        }
    }

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<com.smartbudget.data.dao.TransactionWithCategory>> =
        _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else transactionRepo.searchTransactions(query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Annual summary
    private val _annualData = MutableStateFlow<List<MonthlyTotal>>(emptyList())
    val annualData: StateFlow<List<MonthlyTotal>> = _annualData.asStateFlow()

    fun loadAnnualData(year: Int) {
        viewModelScope.launch {
            val account = _currentAccount.value
            val isConsolidatedMode = _isConsolidated.value
            val results = mutableListOf<MonthlyTotal>()
            for (m in 1..12) {
                val ym = YearMonth.of(year, m)
                val start = ym.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end = ym.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val income: Double
                val expenses: Double
                if (isConsolidatedMode || account == null) {
                    income = app.getDb().transactionDao().getAllTotalByType(TransactionType.INCOME, start, end)
                    expenses = app.getDb().transactionDao().getAllTotalByType(TransactionType.EXPENSE, start, end)
                } else {
                    income = app.getDb().transactionDao().getTotalByType(account.id, TransactionType.INCOME, start, end)
                    expenses = app.getDb().transactionDao().getTotalByType(account.id, TransactionType.EXPENSE, start, end)
                }
                results.add(MonthlyTotal(ym, income, expenses))
            }
            _annualData.value = results
        }
    }

    // Savings Goals
    private val savingsGoalRepo = app.savingsGoalRepository

    val savingsGoals: StateFlow<List<SavingsGoal>> = savingsGoalRepo.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavingsGoal(name: String, targetAmount: Double) {
        viewModelScope.launch {
            savingsGoalRepo.insert(SavingsGoal(name = name, targetAmount = targetAmount))
        }
    }

    fun addAmountToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            val goal = savingsGoalRepo.getGoalById(goalId) ?: return@launch
            savingsGoalRepo.update(goal.copy(currentAmount = goal.currentAmount + amount))
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            savingsGoalRepo.delete(goal)
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
