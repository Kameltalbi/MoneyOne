package com.smartbudget.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.CurrencyData
import com.smartbudget.data.CurrencyInfo
import com.smartbudget.data.entity.Account
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth

data class CategoryFormState(
    val name: String = "",
    val icon: String = "more_horiz",
    val color: Long = 0xFF2196F3,
    val type: TransactionType = TransactionType.EXPENSE,
    val isEditing: Boolean = false,
    val editingId: Long? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SmartBudgetApp
    private val categoryRepo = app.categoryRepository
    private val budgetRepo = app.budgetRepository
    private val accountRepo = app.accountRepository
    private val transactionRepo = app.transactionRepository
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Theme color
    private val _themeColor = MutableStateFlow(prefs.getString("theme_color", "emerald") ?: "emerald")
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    fun setThemeColor(colorName: String) {
        prefs.edit().putString("theme_color", colorName).apply()
        _themeColor.value = colorName
    }

    // Language
    private val _selectedLanguage = MutableStateFlow(prefs.getString("language", "") ?: "")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setLanguage(langCode: String) {
        prefs.edit().putString("language", langCode).apply()
        _selectedLanguage.value = langCode
        val localeList = if (langCode.isEmpty()) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // Balance adjustment
    private val _currentBalance = MutableStateFlow(0.0)
    val currentBalance: StateFlow<Double> = _currentBalance.asStateFlow()

    fun loadCurrentBalance() {
        viewModelScope.launch {
            val account = accountRepo.getDefaultAccount() ?: return@launch
            val now = System.currentTimeMillis() + 86400000L
            transactionRepo.getTotalIncome(account.id, 0L, now).combine(
                transactionRepo.getTotalExpenses(account.id, 0L, now)
            ) { income, expenses -> income - expenses }
                .collect { _currentBalance.value = it }
        }
    }

    fun adjustBalance(newBalance: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val account = accountRepo.getDefaultAccount() ?: return@launch
            val current = _currentBalance.value
            val diff = newBalance - current
            if (diff == 0.0) return@launch

            val type = if (diff > 0) TransactionType.INCOME else TransactionType.EXPENSE
            val transaction = Transaction(
                name = "Ajustement de solde",
                amount = kotlin.math.abs(diff),
                type = type,
                accountId = account.id,
                date = System.currentTimeMillis(),
                note = "Ajustement automatique",
                isValidated = true
            )
            transactionRepo.insert(transaction)
            _currentBalance.value = newBalance
            onSuccess()
        }
    }

    // Accounts
    val allAccounts: StateFlow<List<Account>> = accountRepo.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAccount(name: String, currency: String = "", onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val count = accountRepo.getAccountCount()
            if (count >= 5) {
                onError("max_accounts")
                return@launch
            }
            if (name.isBlank()) return@launch
            accountRepo.insert(Account(name = name, currency = currency))
            onSuccess()
        }
    }

    fun renameAccount(account: Account, newName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (newName.isBlank()) return@launch
            accountRepo.update(account.copy(name = newName))
            onSuccess()
        }
    }

    fun deleteAccount(account: Account, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (account.isDefault) {
                onError("cannot_delete_default")
                return@launch
            }
            val count = accountRepo.getAccountCount()
            if (count <= 1) {
                onError("last_account")
                return@launch
            }
            accountRepo.delete(account)
        }
    }

    fun setDefaultAccount(account: Account) {
        viewModelScope.launch {
            val current = accountRepo.getDefaultAccount()
            if (current != null) {
                accountRepo.update(current.copy(isDefault = false))
            }
            accountRepo.update(account.copy(isDefault = true))
        }
    }

    val allCategories: StateFlow<List<Category>> = categoryRepo.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _categoryForm = MutableStateFlow(CategoryFormState())
    val categoryForm: StateFlow<CategoryFormState> = _categoryForm.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow(YearMonth.now())
    val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

    private val _globalBudgetAmount = MutableStateFlow("")
    val globalBudgetAmount: StateFlow<String> = _globalBudgetAmount.asStateFlow()

    val currentBudget: StateFlow<Budget?> = _selectedYearMonth.flatMapLatest { ym ->
        budgetRepo.getGlobalBudget(DateUtils.yearMonthString(ym))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Currency
    private val _currencyCode = MutableStateFlow(CurrencyFormatter.getCurrencyCode())
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _currencySearch = MutableStateFlow("")
    val currencySearch: StateFlow<String> = _currencySearch.asStateFlow()

    val filteredCurrencies: StateFlow<List<CurrencyInfo>> = _currencySearch.map { query ->
        CurrencyData.search(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CurrencyData.currencies)

    fun updateCurrencySearch(query: String) {
        _currencySearch.value = query
    }

    fun selectCurrency(currency: CurrencyInfo) {
        CurrencyFormatter.saveCurrency(getApplication(), currency.code, currency.symbol)
        _currencyCode.value = currency.code
    }

    init {
        viewModelScope.launch {
            currentBudget.collect { budget ->
                _globalBudgetAmount.value = budget?.amount?.toString() ?: ""
            }
        }
    }

    // Category management
    fun updateCategoryName(name: String) {
        _categoryForm.update { it.copy(name = name) }
    }

    fun updateCategoryIcon(icon: String) {
        _categoryForm.update { it.copy(icon = icon) }
    }

    fun updateCategoryColor(color: Long) {
        _categoryForm.update { it.copy(color = color) }
    }

    fun updateCategoryType(type: TransactionType) {
        _categoryForm.update { it.copy(type = type) }
    }

    fun resetCategoryForm() {
        _categoryForm.value = CategoryFormState()
    }

    fun loadCategory(category: Category) {
        _categoryForm.value = CategoryFormState(
            name = category.name,
            icon = category.icon,
            color = category.color,
            type = category.type,
            isEditing = true,
            editingId = category.id
        )
    }

    fun saveCategory(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val form = _categoryForm.value
            if (form.name.isBlank()) return@launch

            val category = Category(
                id = form.editingId ?: 0,
                name = form.name,
                icon = form.icon,
                color = form.color,
                type = form.type
            )

            if (form.isEditing) {
                categoryRepo.update(category)
            } else {
                categoryRepo.insert(category)
            }
            onSuccess()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepo.delete(category)
        }
    }

    // Category budgets
    val categoryBudgets: StateFlow<List<Budget>> = _selectedYearMonth.flatMapLatest { ym ->
        budgetRepo.getAllBudgetsForMonth(DateUtils.yearMonthString(ym))
    }.map { budgets ->
        budgets.filter { !it.isGlobal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _categoryBudgetAmounts = MutableStateFlow<Map<Long, String>>(emptyMap())
    val categoryBudgetAmounts: StateFlow<Map<Long, String>> = _categoryBudgetAmounts.asStateFlow()

    init {
        viewModelScope.launch {
            categoryBudgets.collect { budgets ->
                val map = budgets.associate { (it.categoryId ?: 0L) to it.amount.toString() }
                _categoryBudgetAmounts.value = map
            }
        }
    }

    fun updateCategoryBudgetAmount(categoryId: Long, amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
        _categoryBudgetAmounts.update { it + (categoryId to filtered) }
    }

    fun saveCategoryBudget(categoryId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val amountStr = _categoryBudgetAmounts.value[categoryId] ?: return@launch
            val amount = amountStr.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch

            val ymStr = DateUtils.yearMonthString(_selectedYearMonth.value)
            val existing = categoryBudgets.value.firstOrNull { it.categoryId == categoryId }

            if (existing != null) {
                budgetRepo.update(existing.copy(amount = amount))
            } else {
                budgetRepo.insert(
                    Budget(
                        amount = amount,
                        yearMonth = ymStr,
                        categoryId = categoryId,
                        isGlobal = false
                    )
                )
            }
            onSuccess()
        }
    }

    fun deleteCategoryBudget(categoryId: Long) {
        viewModelScope.launch {
            val existing = categoryBudgets.value.firstOrNull { it.categoryId == categoryId }
            if (existing != null) {
                budgetRepo.delete(existing)
                _categoryBudgetAmounts.update { it - categoryId }
            }
        }
    }

    // Budget management
    fun updateGlobalBudgetAmount(amount: String) {
        _globalBudgetAmount.value = amount.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
    }

    fun saveGlobalBudget(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val amount = _globalBudgetAmount.value.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch

            val ymStr = DateUtils.yearMonthString(_selectedYearMonth.value)
            val existing = currentBudget.value

            if (existing != null) {
                budgetRepo.update(existing.copy(amount = amount))
            } else {
                budgetRepo.insert(
                    Budget(
                        amount = amount,
                        yearMonth = ymStr,
                        isGlobal = true
                    )
                )
            }
            onSuccess()
        }
    }

    fun navigateBudgetMonth(offset: Int) {
        _selectedYearMonth.value = _selectedYearMonth.value.plusMonths(offset.toLong())
    }
}
