package com.smartbudget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.SmartBudgetApp
import com.smartbudget.widget.BalanceWidgetProvider
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.Recurrence
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TransactionFormState(
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val categoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val recurrence: Recurrence = Recurrence.NONE,
    val isEditing: Boolean = false,
    val editingId: Long? = null
)

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SmartBudgetApp
    private val transactionRepo = app.transactionRepository
    private val categoryRepo = app.categoryRepository
    private val accountRepo = app.accountRepository

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    val allCategories: StateFlow<List<Category>> = categoryRepo.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCategories: StateFlow<List<Category>> =
        combine(_formState, categoryRepo.allCategories) { form, categories ->
            categories.filter { it.type == form.type }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun updateType(type: TransactionType) {
        _formState.update { it.copy(type = type, categoryId = null) }
    }

    fun updateAmount(amount: String) {
        // Allow only valid decimal input
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

    fun updateRecurrence(recurrence: Recurrence) {
        _formState.update { it.copy(recurrence = recurrence) }
    }

    fun resetForm(date: LocalDate = LocalDate.now()) {
        _formState.value = TransactionFormState(date = date)
    }

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId) ?: return@launch
            _formState.value = TransactionFormState(
                name = transaction.name,
                type = transaction.type,
                amount = transaction.amount.toString(),
                categoryId = transaction.categoryId,
                date = DateUtils.fromEpochMillis(transaction.date),
                note = transaction.note,
                recurrence = transaction.recurrence,
                isEditing = true,
                editingId = transaction.id
            )
        }
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val amount = form.amount.toDoubleOrNull() ?: return@launch
            if (amount <= 0) return@launch

            val account = accountRepo.getDefaultAccount() ?: return@launch

            val transaction = Transaction(
                id = form.editingId ?: 0,
                name = form.name,
                amount = amount,
                type = form.type,
                categoryId = form.categoryId,
                accountId = account.id,
                date = DateUtils.toEpochMillis(form.date),
                note = form.note,
                recurrence = form.recurrence
            )

            if (form.isEditing) {
                transactionRepo.update(transaction)
            } else {
                transactionRepo.insert(transaction)
            }

            BalanceWidgetProvider.sendUpdateBroadcast(getApplication())
            // Check budget alerts after saving expense
            if (form.type == TransactionType.EXPENSE) {
                val app = getApplication<com.smartbudget.SmartBudgetApp>()
                app.budgetAlertManager.checkBudgetAlerts(account.id)
            }
            onSuccess()
        }
    }
}
