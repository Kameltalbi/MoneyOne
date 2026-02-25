package com.smartbudget.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbudget.R
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.components.CalendarView
import com.smartbudget.ui.components.MonthSummaryCard
import com.smartbudget.ui.components.TransactionItem
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.DateUtils
import com.smartbudget.ui.util.IconMapper
import com.smartbudget.ui.util.toComposeColor
import com.smartbudget.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onEditRecurringTransaction: (Long, String) -> Unit = { id, _ -> onEditTransaction(id) },
    onSettings: () -> Unit,
    onDashboard: () -> Unit,
    onProUpgrade: () -> Unit,
    onSavingsGoals: () -> Unit = {},
    onSearch: () -> Unit = {},
    onNavigateToSmartInsights: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as SmartBudgetApp
    val isPro by app.billingManager.isPro.collectAsState()

    val currentYearMonth by viewModel.currentYearMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentAccount by viewModel.currentAccount.collectAsStateWithLifecycle()
    val isConsolidated by viewModel.isConsolidated.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val dailyTransactions by viewModel.dailyTransactions.collectAsStateWithLifecycle()
    val monthSummary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val balanceUpToDate by viewModel.balanceUpToDate.collectAsStateWithLifecycle()
    val dayBalances by viewModel.dayBalances.collectAsStateWithLifecycle()

    var showAccountMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showMonthSummary by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var showRecurringEditChoice by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var sortMode by remember { mutableStateOf("date") } // date, amount, category
    var showDuplicatedSnackbar by remember { mutableStateOf(false) }
    var duplicatingTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }

    val sortedTransactions = remember(dailyTransactions, sortMode) {
        when (sortMode) {
            "amount" -> dailyTransactions.sortedByDescending { it.amount }
            "category" -> dailyTransactions.sortedBy { it.categoryName ?: "" }
            else -> dailyTransactions
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
            ) {
                // Line 1: Title + main action icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MoneyOne",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Search
                    IconButton(onClick = onSearch, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
                    }
                    
                    // Pro features - only show if user has Pro
                    if (isPro) {
                        // Dashboard
                        IconButton(onClick = onDashboard, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.PieChart, contentDescription = stringResource(R.string.dashboard),
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
                        }
                        // Smart Insights
                        IconButton(onClick = onNavigateToSmartInsights, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Insights, contentDescription = "Smart Insights",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
                        }
                        // Savings goals
                        IconButton(onClick = onSavingsGoals, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = stringResource(R.string.savings_goals_title),
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
                        }
                    } else {
                        // FREE mode: Single "Go Pro" button
                        IconButton(onClick = onProUpgrade, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = "Go Pro",
                                tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                        }
                    }
                    
                    // Settings
                    IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
                    }
                }

                // Line 2: Account selector + summary toggle + export
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .offset(y = (-4).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showAccountMenu = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConsolidated) stringResource(R.string.all_accounts)
                                   else currentAccount?.name ?: stringResource(R.string.account),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    // Month summary toggle
                    IconButton(onClick = { showMonthSummary = !showMonthSummary }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.BarChart, contentDescription = stringResource(R.string.month_summary),
                            tint = if (showMonthSummary) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp))
                    }
                    // Export (PDF/CSV)
                    Box {
                        IconButton(
                            onClick = { 
                                if (isPro) {
                                    showExportMenu = true
                                } else {
                                    onProUpgrade()
                                }
                            }, 
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Share, 
                                contentDescription = "Export",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📄 Export PDF") },
                                onClick = {
                                    val monthLabel = DateUtils.formatMonthYear(currentYearMonth)
                                    val transactions = viewModel.monthlyTransactions.value
                                    com.smartbudget.ui.util.PdfExporter.exportAndShare(context, transactions, monthLabel)
                                    showExportMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📊 Export CSV") },
                                onClick = {
                                    val transactions = viewModel.monthlyTransactions.value
                                    val categoryMap = transactions.associate { 
                                        it.categoryId to (it.categoryName ?: "") 
                                    }.filterKeys { it != null } as Map<Long, String>
                                    val accountMap = mapOf(currentAccount?.id to (currentAccount?.name ?: ""))
                                        .filterKeys { it != null } as Map<Long, String>
                                    
                                    val intent = com.smartbudget.util.CsvExporter.exportTransactionsToCsv(
                                        context,
                                        transactions.map { it.toTransaction() },
                                        categoryMap,
                                        accountMap
                                    )
                                    intent?.let { context.startActivity(Intent.createChooser(it, "Export CSV")) }
                                    showExportMenu = false
                                }
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showAccountMenu,
                        onDismissRequest = { showAccountMenu = false }
                    ) {
                        if (isPro && accounts.size > 1) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.all_accounts),
                                        fontWeight = if (isConsolidated) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.selectAllAccounts()
                                    showAccountMenu = false
                                },
                                leadingIcon = {
                                    if (isConsolidated) {
                                        Icon(Icons.Filled.Check, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                            Divider()
                        }
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    viewModel.selectAccount(account)
                                    showAccountMenu = false
                                },
                                leadingIcon = {
                                    if (!isConsolidated && account.id == currentAccount?.id) {
                                        Icon(Icons.Filled.Check, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_transaction))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Calendar
            item {
                CalendarView(
                    yearMonth = currentYearMonth,
                    selectedDate = selectedDate,
                    dayBalances = dayBalances,
                    onDateSelected = { viewModel.selectDate(it) },
                    onPreviousMonth = { viewModel.navigateMonth(-1) },
                    onNextMonth = { viewModel.navigateMonth(1) }
                )
            }

            // Daily balance under calendar
            item {
                val balanceColor = if (balanceUpToDate >= 0)
                    com.smartbudget.ui.theme.IncomeGreen
                else
                    com.smartbudget.ui.theme.ExpenseRed

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.balance_at_day, DateUtils.formatDateDayOnly(selectedDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = com.smartbudget.ui.util.CurrencyFormatter.format(balanceUpToDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                }
            }

            // Month summary (toggle)
            if (showMonthSummary) {
                item {
                    AnimatedVisibility(
                        visible = showMonthSummary,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        MonthSummaryCard(summary = monthSummary)
                    }
                }
            }

            // Daily transactions (hidden when month summary is shown)
            if (!showMonthSummary) {
            item {
                Text(
                    text = stringResource(R.string.transactions_for_day, DateUtils.formatDateDayOnly(selectedDate)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (dailyTransactions.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_transaction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(sortedTransactions, key = { it.id }) { transaction ->
                    val dismissState = rememberDismissState(
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                DismissValue.DismissedToStart -> {
                                    // Swipe left -> delete
                                    showDeleteConfirm = transaction
                                    false
                                }
                                DismissValue.DismissedToEnd -> {
                                    // Swipe right -> duplicate with date picker
                                    duplicatingTransaction = transaction
                                    false
                                }
                                else -> false
                            }
                        }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val direction = dismissState.dismissDirection
                            val swipeBgColor = when (direction) {
                                DismissDirection.EndToStart -> ExpenseRed
                                DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            }
                            val icon = when (direction) {
                                DismissDirection.EndToStart -> Icons.Filled.Delete
                                DismissDirection.StartToEnd -> Icons.Filled.ContentCopy
                                else -> Icons.Filled.Delete
                            }
                            val alignment = when (direction) {
                                DismissDirection.EndToStart -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(swipeBgColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                if (direction != null) {
                                    Icon(icon, contentDescription = null, tint = swipeBgColor)
                                }
                            }
                        },
                        dismissContent = {
                            TransactionItem(
                                transaction = transaction,
                                onToggleValidation = { viewModel.toggleTransactionValidation(it) },
                                onClick = { selectedTransaction = transaction }
                            )
                        },
                        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd)
                    )
                }
            }
            } // end if (!showMonthSummary)

            // Bottom spacer for FAB
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // Transaction action dialog
    selectedTransaction?.let { txn ->
        val isIncome = txn.type == TransactionType.INCOME
        val amountColor = if (isIncome) IncomeGreen else ExpenseRed
        val amountPrefix = if (isIncome) "+" else "-"
        val catColor = txn.categoryColor?.toComposeColor() ?: MaterialTheme.colorScheme.primary

        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(catColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconMapper.getIcon(txn.categoryIcon ?: "more_horiz"),
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = txn.name.ifBlank { txn.categoryName ?: stringResource(R.string.no_category) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "\u200E$amountPrefix${CurrencyFormatter.format(txn.amount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Edit button
                    Surface(
                        onClick = {
                            if (txn.recurringId != null) {
                                showRecurringEditChoice = txn
                                selectedTransaction = null
                            } else {
                                val id = txn.id
                                selectedTransaction = null
                                onEditTransaction(id)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.edit_transaction),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Duplicate button
                    Surface(
                        onClick = {
                            duplicatingTransaction = txn
                            selectedTransaction = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.duplicate_transaction),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Delete button
                    Surface(
                        onClick = {
                            showDeleteConfirm = txn
                            selectedTransaction = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = ExpenseRed.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.delete_transaction),
                                style = MaterialTheme.typography.bodyLarge,
                                color = ExpenseRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedTransaction = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation
    showDeleteConfirm?.let { txn ->
        if (txn.recurringId != null) {
            // Recurring transaction: show 3-choice dialog
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showDeleteConfirm = null }
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.delete_recurring_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteSingleOccurrence(txn.id)
                                showDeleteConfirm = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.delete_recurring_this_only))
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteFutureOccurrences(txn.id)
                                showDeleteConfirm = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.delete_recurring_this_and_future))
                        }
                        Button(
                            onClick = {
                                viewModel.deleteEntireSeries(txn.id)
                                showDeleteConfirm = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                        ) {
                            Text(stringResource(R.string.delete_recurring_all))
                        }
                        TextButton(
                            onClick = { showDeleteConfirm = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        } else {
            // Non-recurring: simple confirm dialog
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text(stringResource(R.string.delete_transaction)) },
                text = { Text(stringResource(R.string.delete_transaction_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTransaction(txn.id)
                            showDeleteConfirm = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    // Recurring edit choice dialog
    showRecurringEditChoice?.let { txn ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showRecurringEditChoice = null }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.edit_recurring_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            val id = txn.id
                            showRecurringEditChoice = null
                            onEditRecurringTransaction(id, "SINGLE")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.edit_recurring_this_only))
                    }
                    OutlinedButton(
                        onClick = {
                            val id = txn.id
                            showRecurringEditChoice = null
                            onEditRecurringTransaction(id, "FUTURE")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.edit_recurring_this_and_future))
                    }
                    Button(
                        onClick = {
                            val id = txn.id
                            showRecurringEditChoice = null
                            onEditRecurringTransaction(id, "ALL")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.edit_recurring_all))
                    }
                    TextButton(
                        onClick = { showRecurringEditChoice = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }

    // Duplicated snackbar
    if (showDuplicatedSnackbar) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showDuplicatedSnackbar = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showDuplicatedSnackbar = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        ) {
            Text(stringResource(R.string.transaction_duplicated))
        }
    }

    // DatePicker for duplicate transaction
    duplicatingTransaction?.let { txn ->
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtils.toUtcMillis(selectedDate)
        )
        DatePickerDialog(
            onDismissRequest = { duplicatingTransaction = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = DateUtils.fromUtcMillis(millis)
                        viewModel.duplicateTransactionToDate(txn.id, DateUtils.toEpochMillis(newDate))
                        showDuplicatedSnackbar = true
                    }
                    duplicatingTransaction = null
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { duplicatingTransaction = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
