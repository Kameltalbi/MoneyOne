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
    val recurringEditMode: RecurringEditMode? = null
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

    fun resetForm(date: LocalDate = LocalDate.now()) {
        _formState.value = TransactionFormState(date = date)
    }

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId, userId) ?: return@launch
            _formState.value = TransactionFormState(
                name = transaction.name,
                type = transaction.type,
                amount = transaction.amount.toString(),
                categoryId = transaction.categoryId,
                date = DateUtils.fromEpochMillis(transaction.date),
                note = transaction.note,
                isEditing = true,
                editingId = transaction.id,
                recurringId = transaction.recurringId
            )
        }
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val amount = form.amount.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch

            val account = accountRepo.getDefaultAccount(userId) ?: return@launch
            val dateMillis = DateUtils.toEpochMillis(form.date)

            if (form.isEditing) {
                val existing = transactionRepo.getTransactionById(form.editingId!!, userId) ?: return@launch
                transactionRepo.update(existing.copy(
                    name = form.name,
                    amount = amount,
                    type = form.type,
                    categoryId = form.categoryId,
                    date = dateMillis,
                    note = form.note
                ))
            } else {
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
                } else {
                    transactionRepo.insert(transaction)
                }
            }

            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
            if (form.type == TransactionType.EXPENSE) {
                val app = getApplication<SmartBudgetApp>()
                app.budgetAlertManager.checkBudgetAlerts(account.id)
            }
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

            android.util.Log.d("TxnVM", "modifySingleOccurrence: id=$id amount=$amount recurringId=${form.recurringId}")
            val existing = transactionRepo.getTransactionById(id, userId) ?: return@launch
            android.util.Log.d("TxnVM", "modifySingleOccurrence: existing.id=${existing.id} existing.amount=${existing.amount} existing.recurringId=${existing.recurringId}")
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

            // Update this transaction
            val existing = transactionRepo.getTransactionById(id, userId) ?: return@launch
            transactionRepo.update(existing.copy(
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                date = dateMillis,
                note = form.note
            ))

            // Update all future unmodified occurrences
            transactionRepo.updateFutureUnmodifiedOccurrences(
                userId = userId,
                recurringId = recurringId,
                fromDate = dateMillis,
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                note = form.note
            )

            // Split the recurring rule: end old rule, create new one
            val oldRecurring = recurringRepo.getById(recurringId, userId)
            if (oldRecurring != null) {
                recurringRepo.update(oldRecurring.copy(endDate = dateMillis - 1))
                recurringRepo.insert(oldRecurring.copy(
                    id = 0,
                    startDate = dateMillis,
                    amount = amount,
                    name = form.name,
                    categoryId = form.categoryId,
                    note = form.note
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

            // Update this transaction
            val existing = transactionRepo.getTransactionById(id, userId) ?: return@launch
            transactionRepo.update(existing.copy(
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                date = DateUtils.toEpochMillis(form.date),
                note = form.note
            ))

            // Update recurring rule
            val recurring = recurringRepo.getById(recurringId, userId)
            if (recurring != null) {
                recurringRepo.update(recurring.copy(
                    name = form.name,
                    amount = amount,
                    type = form.type,
                    categoryId = form.categoryId,
                    note = form.note
                ))
            }

            // Update all future unmodified occurrences from the start
            transactionRepo.updateFutureUnmodifiedOccurrences(
                userId = userId,
                recurringId = recurringId,
                fromDate = 0,
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                note = form.note
            )

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
