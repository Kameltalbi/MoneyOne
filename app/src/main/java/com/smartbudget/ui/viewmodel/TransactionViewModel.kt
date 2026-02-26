package com.smartbudget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.UserManager
import com.smartbudget.widget.BalanceWidgetProvider
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.Frequency
import com.smartbudget.data.entity.RecurringTransaction
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class RecurringEditMode {
    SINGLE,      // This transaction only
    FUTURE,      // This and all future
    ALL          // Entire series
}

data class TransactionFormState(
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val categoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val frequency: Frequency? = null,
    val frequencyInterval: Int = 1,
    val isEditing: Boolean = false,
    val editingId: Long? = null,
    val recurringId: Long? = null,
    val recurringEditMode: RecurringEditMode? = null,
    val recurringFrequency: Frequency? = null,  // For displaying frequency when editing
    val selectedAccountId: Long? = null,
    val destinationAccountId: Long? = null,  // For transfers
    val customCurrency: String? = null  // For Pro: override account currency
)

class TransactionViewModel(
    application: Application,
    private val userManager: UserManager
) : AndroidViewModel(application) {
    private val app = application as SmartBudgetApp
    private val transactionRepo = app.transactionRepository
    private val categoryRepo = app.categoryRepository
    private val accountRepo = app.accountRepository
    private val recurringRepo = app.recurringRepository
    
    private val userId: String
        get() = userManager.getCurrentUserId()

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    fun requestSave(onNavigateBack: () -> Unit) {
        val form = _formState.value
        if (form.isEditing && form.recurringId != null && form.recurringEditMode != null) {
            // Mode already chosen from MainScreen dialog
            when (form.recurringEditMode) {
                RecurringEditMode.SINGLE -> modifySingleOccurrence(onNavigateBack)
                RecurringEditMode.FUTURE -> modifyFutureOccurrences(onNavigateBack)
                RecurringEditMode.ALL -> modifyEntireSeries(onNavigateBack)
            }
        } else {
            saveTransaction(onNavigateBack)
        }
    }

    fun setRecurringEditMode(mode: RecurringEditMode) {
        _formState.update { it.copy(recurringEditMode = mode) }
    }

    val allCategories: StateFlow<List<Category>> = flow { emit(userId) }
        .flatMapLatest { categoryRepo.getAllCategories(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts = flow { emit(userId) }
        .flatMapLatest { accountRepo.getAllAccounts(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCategories: StateFlow<List<Category>> =
        combine(_formState, allCategories) { form, categories ->
            categories.filter { it.type == form.type }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun updateType(type: TransactionType) {
        _formState.update { it.copy(type = type, categoryId = null) }
    }

    fun updateAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
        _formState.update { it.copy(amount = filtered) }
    }

    fun updateCategory(categoryId: Long) {
        _formState.update { it.copy(categoryId = categoryId) }
    }

    fun updateDate(date: LocalDate) {
        _formState.update { it.copy(date = date) }
    }

    fun updateNote(note: String) {
        _formState.update { it.copy(note = note) }
    }

    fun updateFrequency(frequency: Frequency?, interval: Int = 1) {
        _formState.update { it.copy(frequency = frequency, frequencyInterval = interval) }
    }

    fun updateDestinationAccount(accountId: Long?) {
        _formState.update { it.copy(destinationAccountId = accountId) }
    }

    fun updateCustomCurrency(currency: String?) {
        _formState.update { it.copy(customCurrency = currency) }
    }

    fun resetForm(date: LocalDate = LocalDate.now(), accountId: Long? = null) {
        _formState.value = TransactionFormState(date = date, selectedAccountId = accountId)
    }

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            val currentMode = _formState.value.recurringEditMode  // Preserve mode if already set
            
            // Load recurring info if this is a recurring transaction
            val recurringFrequency = if (transaction.recurringId != null) {
                recurringRepo.getById(transaction.recurringId, userId)?.frequency
            } else null
            
            _formState.value = TransactionFormState(
                name = transaction.name,
                type = transaction.type,
                amount = transaction.amount.toString(),
                categoryId = transaction.categoryId,
                date = DateUtils.fromEpochMillis(transaction.date),
                note = transaction.note,
                isEditing = true,
                editingId = transaction.id,
                recurringId = transaction.recurringId,
                recurringEditMode = currentMode,  // Preserve the mode
                recurringFrequency = recurringFrequency
            )
        }
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val amount = form.amount.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch

            // Use selected account if available, otherwise use default
            val account = if (form.selectedAccountId != null) {
                accountRepo.getAccountById(form.selectedAccountId, userId)
            } else {
                accountRepo.getDefaultAccount(userId)
            } ?: return@launch
            val dateMillis = DateUtils.toEpochMillis(form.date)

            if (form.isEditing) {
                val existing = transactionRepo.getTransactionById(form.editingId!!, userId) ?: return@launch
                transactionRepo.update(existing.copy(
                    name = form.name,
                    amount = amount,
                    type = form.type,
                    categoryId = form.categoryId,
                    date = dateMillis,
                    note = form.note,
                    destinationAccountId = form.destinationAccountId
                ))
            } else {
                // Handle TRANSFER type: create 2 linked transactions
                if (form.type == TransactionType.TRANSFER && form.destinationAccountId != null) {
                    val destinationAccount = accountRepo.getAccountById(form.destinationAccountId, userId)
                    if (destinationAccount != null) {
                        // Debit from source account (EXPENSE-like)
                        val debitTransaction = Transaction(
                            name = form.name,
                            amount = amount,
                            type = TransactionType.TRANSFER,
                            categoryId = null,  // No category for transfers
                            accountId = account.id,
                            destinationAccountId = destinationAccount.id,
                            date = dateMillis,
                            note = form.note,
                            userId = userId
                        )
                        transactionRepo.insert(debitTransaction)
                    }
                } else {
                    // Regular INCOME or EXPENSE transaction
                    val transaction = Transaction(
                        name = form.name,
                        amount = amount,
                        type = form.type,
                        categoryId = form.categoryId,
                        accountId = account.id,
                        date = dateMillis,
                        note = form.note,
                        userId = userId
                    )

                    if (form.frequency != null) {
                        // Create recurring rule + first occurrence
                        val recurringId = recurringRepo.insert(
                            RecurringTransaction(
                                name = form.name,
                                amount = amount,
                                type = form.type,
                                categoryId = form.categoryId,
                                accountId = account.id,
                                note = form.note,
                                startDate = dateMillis,
                                frequency = form.frequency,
                                interval = form.frequencyInterval,
                                userId = userId
                            )
                        )
                        transactionRepo.insert(transaction.copy(recurringId = recurringId))
                        
                        // Generate occurrences immediately for current month
                        val recurring = recurringRepo.getById(recurringId, userId)
                        if (recurring != null) {
                            val currentMonth = java.time.YearMonth.now()
                            app.recurringGenerator.generateUpToMonth(recurring, currentMonth)
                        }
                    } else {
                        transactionRepo.insert(transaction)
                    }
                }
            }

            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
            if (form.type == TransactionType.EXPENSE) {
                val app = getApplication<SmartBudgetApp>()
                app.budgetAlertManager.checkBudgetAlerts(account.id)
            }
            // Reset custom currency after save to restore account's default currency
            _formState.update { it.copy(customCurrency = null) }
            onSuccess()
        }
    }

    // Edit only this single occurrence (mark as modified)
    fun modifySingleOccurrence(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val amount = form.amount.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch
            val id = form.editingId ?: return@launch

            val existing = transactionRepo.getTransactionById(id, userId) ?: return@launch
            transactionRepo.update(existing.copy(
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                date = DateUtils.toEpochMillis(form.date),
                note = form.note,
                isModified = true
            ))

            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
            val account = accountRepo.getDefaultAccount(userId)
            if (form.type == TransactionType.EXPENSE && account != null) {
                val app = getApplication<SmartBudgetApp>()
                app.budgetAlertManager.checkBudgetAlerts(account.id)
            }
            onSuccess()
        }
    }

    // Edit this and all future unmodified occurrences
    fun modifyFutureOccurrences(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val amount = form.amount.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch
            val recurringId = form.recurringId ?: return@launch
            val id = form.editingId ?: return@launch
            val dateMillis = DateUtils.toEpochMillis(form.date)

            // Split the recurring rule: end old rule, create new one
            val oldRecurring = recurringRepo.getById(recurringId, userId)
            var newRecurringId = recurringId
            if (oldRecurring != null) {
                // End the old recurring rule at the day before this transaction
                recurringRepo.update(oldRecurring.copy(endDate = dateMillis - 1))
                
                // Create new recurring rule starting from this transaction
                newRecurringId = recurringRepo.insert(oldRecurring.copy(
                    id = 0,
                    startDate = dateMillis,
                    amount = amount,
                    name = form.name,
                    categoryId = form.categoryId,
                    note = form.note
                ))
            }

            // Update this transaction and link it to the new recurring rule
            val existing = transactionRepo.getTransactionById(id, userId) ?: return@launch
            transactionRepo.update(existing.copy(
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                date = dateMillis,
                note = form.note,
                recurringId = newRecurringId,
                isModified = false
            ))

            // Get all future occurrences from the old rule and update them to the new rule
            val futureOccurrences = transactionRepo.getAllOccurrencesByRecurringId(userId, recurringId)
                .filter { it.date >= dateMillis && it.id != id }
            
            futureOccurrences.forEach { occurrence ->
                transactionRepo.update(occurrence.copy(
                    name = form.name,
                    amount = amount,
                    type = form.type,
                    categoryId = form.categoryId,
                    note = form.note,
                    recurringId = newRecurringId,
                    isModified = false
                ))
            }

            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
            val account = accountRepo.getDefaultAccount(userId)
            if (form.type == TransactionType.EXPENSE && account != null) {
                val app = getApplication<SmartBudgetApp>()
                app.budgetAlertManager.checkBudgetAlerts(account.id)
            }
            onSuccess()
        }
    }

    // Edit the entire series (update the recurring rule + all unmodified occurrences)
    fun modifyEntireSeries(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val amount = form.amount.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch
            val recurringId = form.recurringId ?: return@launch
            val id = form.editingId ?: return@launch

            val existing = transactionRepo.getTransactionById(id, userId) ?: return@launch
            val originalDateMillis = existing.date
            val newDateMillis = DateUtils.toEpochMillis(form.date)

            // First: Update the recurring rule with ALL new values including startDate
            val recurring = recurringRepo.getById(recurringId, userId)
            if (recurring == null) return@launch
            
            val oldStartDate = java.time.Instant.ofEpochMilli(recurring.startDate)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            val newStartDate = java.time.Instant.ofEpochMilli(newDateMillis)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            
            recurringRepo.update(recurring.copy(
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                note = form.note,
                startDate = newDateMillis  // NEW start date
            ))

            // Second: Delete ALL unmodified occurrences (they will be regenerated with new dates)
            val allOccurrences = transactionRepo.getAllOccurrencesByRecurringId(userId, recurringId)
            android.util.Log.d("TransactionVM", "modifyEntireSeries: Found ${allOccurrences.size} occurrences for recurringId=$recurringId")
            
            var deletedCount = 0
            var updatedCount = 0
            allOccurrences.forEach { occurrence ->
                android.util.Log.d("TransactionVM", "Processing occurrence id=${occurrence.id}, date=${occurrence.date}, isModified=${occurrence.isModified}")
                if (!occurrence.isModified) {
                    // Delete all unmodified occurrences - they will be regenerated
                    transactionRepo.delete(occurrence)
                    deletedCount++
                    android.util.Log.d("TransactionVM", "Deleted occurrence id=${occurrence.id}")
                } else {
                    // Modified occurrences: update fields only (keep their custom dates)
                    transactionRepo.update(occurrence.copy(
                        name = form.name,
                        amount = amount,
                        type = form.type,
                        categoryId = form.categoryId,
                        note = form.note
                    ))
                    updatedCount++
                    android.util.Log.d("TransactionVM", "Updated modified occurrence id=${occurrence.id}")
                }
            }
            android.util.Log.d("TransactionVM", "modifyEntireSeries: Deleted $deletedCount, Updated $updatedCount occurrences")
            
            // Third: Regenerate ALL occurrences from the NEW startDate
            val updatedRecurring = recurringRepo.getById(recurringId, userId)
            if (updatedRecurring != null) {
                // Generate from the new startDate month to 12 months ahead
                val startMonth = newStartDate.let { java.time.YearMonth.from(it) }
                val currentMonth = java.time.YearMonth.now()
                val beginMonth = if (startMonth.isBefore(currentMonth)) startMonth else currentMonth
                val endMonth = currentMonth.plusMonths(12)
                
                var month = beginMonth
                while (!month.isAfter(endMonth)) {
                    app.recurringGenerator.generateUpToMonth(updatedRecurring, month)
                    month = month.plusMonths(1)
                }
            }

            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
            val account = accountRepo.getDefaultAccount(userId)
            if (form.type == TransactionType.EXPENSE && account != null) {
                val app = getApplication<SmartBudgetApp>()
                app.budgetAlertManager.checkBudgetAlerts(account.id)
            }
            onSuccess()
        }
    }
}
