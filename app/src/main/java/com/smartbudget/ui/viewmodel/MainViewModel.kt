package com.smartbudget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.RecurringGenerator
import com.smartbudget.data.UserManager
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
class MainViewModel(
    application: Application,
    private val userManager: UserManager
) : AndroidViewModel(application) {
    private val app = application as SmartBudgetApp
    private val accountRepo = app.accountRepository
    private val transactionRepo = app.transactionRepository
    private val budgetRepo = app.budgetRepository
    
    private val userId: String
        get() = userManager.getCurrentUserId()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentAccount = MutableStateFlow<Account?>(null)
    val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    private val _isConsolidated = MutableStateFlow(false)
    val isConsolidated: StateFlow<Boolean> = _isConsolidated.asStateFlow()

    val accounts: StateFlow<List<Account>> = flow { emit(userId) }
        .flatMapLatest { accountRepo.getAllAccounts(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val defaultAccount = accountRepo.getDefaultAccount(userId)
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
        // Reset selected date to same day in new month (or first day)
        val currentDay = _selectedDate.value.dayOfMonth
        val maxDay = newMonth.lengthOfMonth()
        _selectedDate.value = newMonth.atDay(minOf(currentDay, maxDay))
        viewModelScope.launch {
            generateRecurringForMonth(newMonth)
        }
    }

    private suspend fun generateRecurringForMonth(month: YearMonth) {
        val activeRules = recurringRepo.getActiveRecurring(userId)
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
                transactionRepo.getAllTransactionsForPeriod(userId, start, end)
            } else if (account == null) {
                flowOf(emptyList())
            } else {
                transactionRepo.getTransactionsForPeriod(userId, account.id, start, end)
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
                transactionRepo.getAllTransactionsForDay(userId, start, end)
            } else if (account == null) {
                flowOf(emptyList())
            } else {
                transactionRepo.getTransactionsForDay(userId, account.id, start, end)
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
                    transactionRepo.getAllTotalIncome(userId, start, end),
                    transactionRepo.getAllTotalExpenses(userId, start, end),
                    budgetRepo.getGlobalBudget(userId, ymStr)
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
                    transactionRepo.getTotalIncome(userId, account.id, start, end),
                    transactionRepo.getTotalExpenses(userId, account.id, start, end),
                    budgetRepo.getGlobalBudget(userId, ymStr)
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

    // Balance up to selected date (cumulative from all time)
    val balanceUpToDate: StateFlow<Double> =
        combine(_currentAccount, _selectedDate, _isConsolidated) { account, date, consolidated ->
            Triple(account, date, consolidated)
        }.flatMapLatest { (account, date, consolidated) ->
            val end = DateUtils.dayEnd(date)
            if (consolidated) {
                combine(
                    transactionRepo.getAllTotalIncome(userId, null, end),
                    transactionRepo.getAllTotalExpenses(userId, null, end)
                ) { income, expenses -> income - expenses }
            } else if (account == null) {
                flowOf(0.0)
            } else {
                combine(
                    transactionRepo.getTotalIncome(userId, account.id, null, end),
                    transactionRepo.getTotalExpenses(userId, account.id, null, end)
                ) { income, expenses -> income - expenses }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category budgets for the month
    val categoryBudgets: StateFlow<List<Budget>> = _currentYearMonth.flatMapLatest { ym ->
        budgetRepo.getAllBudgetsForMonth(userId, DateUtils.yearMonthString(ym))
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
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            transactionRepo.update(transaction.copy(isValidated = !transaction.isValidated))
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            if (transaction.recurringId != null) {
                // Soft delete for recurring
                transactionRepo.softDelete(userId, transactionId)
            } else {
                transactionRepo.delete(transaction)
            }
            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun deleteSingleOccurrence(transactionId: Long) {
        viewModelScope.launch {
            transactionRepo.softDelete(userId, transactionId)
            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun deleteFutureOccurrences(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            val recurringId = transaction.recurringId ?: return@launch
            transactionRepo.softDeleteFutureOccurrences(userId, recurringId, transaction.date)
            // End the recurring rule
            val recurring = recurringRepo.getById(recurringId, userId)
            if (recurring != null) {
                recurringRepo.update(recurring.copy(endDate = transaction.date - 1))
            }
            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun deleteEntireSeries(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            val recurringId = transaction.recurringId ?: return@launch
            transactionRepo.softDeleteAllOccurrences(userId, recurringId)
            // Deactivate the recurring rule
            val recurring = recurringRepo.getById(recurringId, userId)
            if (recurring != null) {
                recurringRepo.update(recurring.copy(isActive = false))
            }
            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun duplicateTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            transactionRepo.insert(transaction.copy(id = 0))
        }
    }

    fun duplicateTransactionToDate(transactionId: Long, dateMillis: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            transactionRepo.insert(transaction.copy(id = 0, date = dateMillis))
        }
    }

    fun createFirstAccount(name: String, initialBalance: Double) {
        viewModelScope.launch {
            val accountId = accountRepo.insert(Account(name = name, isDefault = true, userId = userId))
            _currentAccount.value = accountRepo.getDefaultAccount(userId)
            if (initialBalance > 0) {
                val transaction = Transaction(
                    name = "Solde initial",
                    amount = initialBalance,
                    type = TransactionType.INCOME,
                    accountId = accountId,
                    date = System.currentTimeMillis(),
                    note = "Solde initial du compte",
                    isValidated = true,
                    userId = userId
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
            if (dayOfMonth >= daysInMonth) {
                _monthForecast.value = MonthForecast()
                return@launch
            }

            val start = DateUtils.monthStart(ym)
            val nowEnd = DateUtils.dayEnd(today)
            val monthEnd = DateUtils.monthEnd(ym)
            val account = _currentAccount.value
            val consolidated = _isConsolidated.value

            // Current month actuals
            val currentIncome: Double
            val currentExpenses: Double
            if (consolidated || account == null) {
                currentIncome = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.INCOME, start, nowEnd)
                currentExpenses = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.EXPENSE, start, nowEnd)
            } else {
                currentIncome = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.INCOME, start, nowEnd)
                currentExpenses = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.EXPENSE, start, nowEnd)
            }

            // Get remaining recurring transactions for this month
            val tomorrow = today.plusDays(1)
            val tomorrowStart = DateUtils.dayStart(tomorrow)
            val remainingRecurringIncome: Double
            val remainingRecurringExpenses: Double
            if (consolidated || account == null) {
                remainingRecurringIncome = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.INCOME, tomorrowStart, monthEnd)
                remainingRecurringExpenses = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.EXPENSE, tomorrowStart, monthEnd)
            } else {
                remainingRecurringIncome = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.INCOME, tomorrowStart, monthEnd)
                remainingRecurringExpenses = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.EXPENSE, tomorrowStart, monthEnd)
            }

            // Calculate daily averages for non-recurring transactions only
            val daysRemaining = daysInMonth - dayOfMonth
            val avgDailyNonRecurringExpense = if (dayOfMonth > 0) (currentExpenses - remainingRecurringExpenses) / dayOfMonth else 0.0
            val avgDailyNonRecurringIncome = if (dayOfMonth > 0) (currentIncome - remainingRecurringIncome) / dayOfMonth else 0.0

            // Project remaining non-recurring transactions
            val projectedRemainingExpenses = avgDailyNonRecurringExpense * daysRemaining
            val projectedRemainingIncome = avgDailyNonRecurringIncome * daysRemaining

            // Total projections
            val projectedExpenses = currentExpenses + projectedRemainingExpenses
            val projectedIncome = currentIncome + projectedRemainingIncome

            _monthForecast.value = MonthForecast(
                projectedExpenses = projectedExpenses,
                projectedIncome = projectedIncome,
                projectedBalance = projectedIncome - projectedExpenses,
                daysElapsed = dayOfMonth,
                daysInMonth = daysInMonth,
                dailyExpenseRate = avgDailyNonRecurringExpense,
                dailyIncomeRate = avgDailyNonRecurringIncome
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
                income = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.INCOME, start, end)
                expenses = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.EXPENSE, start, end)
            } else {
                income = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.INCOME, start, end)
                expenses = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.EXPENSE, start, end)
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
            else transactionRepo.searchTransactions(userId, query)
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
                    income = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.INCOME, start, end)
                    expenses = app.getDb().transactionDao().getAllTotalByType(userId, TransactionType.EXPENSE, start, end)
                } else {
                    income = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.INCOME, start, end)
                    expenses = app.getDb().transactionDao().getTotalByType(userId, account.id, TransactionType.EXPENSE, start, end)
                }
                results.add(MonthlyTotal(ym, income, expenses))
            }
            _annualData.value = results
        }
    }

    // Savings Goals
    private val savingsGoalRepo = app.savingsGoalRepository

    val savingsGoals: StateFlow<List<SavingsGoal>> = flow { emit(userId) }
        .flatMapLatest { savingsGoalRepo.getAllGoals(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavingsGoal(name: String, targetAmount: Double, isPro: Boolean, onError: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            if (!isPro) {
                val currentGoals = savingsGoalRepo.getAllGoals(userId).first()
                if (currentGoals.size >= 1) {
                    onError?.invoke("free_max_savings_goals")
                    return@launch
                }
            }
            savingsGoalRepo.insert(SavingsGoal(name = name, targetAmount = targetAmount, userId = userId))
        }
    }

    fun addAmountToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            val goal = savingsGoalRepo.getGoalById(goalId, userId) ?: return@launch
            savingsGoalRepo.update(goal.copy(currentAmount = goal.currentAmount + amount))
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            savingsGoalRepo.deleteGoal(goal)
        }
    }

    fun setInitialBalance(amount: Double) {
        if (amount == 0.0) return
        viewModelScope.launch {
            val account = accountRepo.getDefaultAccount(userId) ?: return@launch
            val transaction = Transaction(
                name = "Solde initial",
                amount = amount,
                type = TransactionType.INCOME,
                accountId = account.id,
                date = System.currentTimeMillis(),
                note = "Solde initial du compte",
                isValidated = true,
                userId = userId
            )
            transactionRepo.insert(transaction)
        }
    }
}
