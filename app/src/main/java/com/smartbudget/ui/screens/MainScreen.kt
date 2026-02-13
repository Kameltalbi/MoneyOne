package com.smartbudget.ui.screens

import android.content.Context
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
    onSettings: () -> Unit,
    onDashboard: () -> Unit,
    onProUpgrade: () -> Unit,
    onSavingsGoals: () -> Unit = {},
    onSearch: () -> Unit = {}
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
    var selectedTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TransactionWithCategory?>(null) }
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
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Line 1: Title + action icons
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
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Search
                    IconButton(onClick = onSearch) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Month summary toggle
                    IconButton(onClick = { showMonthSummary = !showMonthSummary }) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = stringResource(R.string.month_summary),
                            tint = if (showMonthSummary) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Overflow menu
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.menu))
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dashboard) + if (!isPro) " ⭐" else "") },
                            onClick = {
                                showOverflowMenu = false
                                if (isPro) onDashboard() else onProUpgrade()
                            },
                            leadingIcon = { Icon(Icons.Filled.PieChart, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.savings_goals_title) + if (!isPro) " ⭐" else "") },
                            onClick = {
                                showOverflowMenu = false
                                if (isPro) onSavingsGoals() else onProUpgrade()
                            },
                            leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_csv)) },
                            onClick = {
                                showOverflowMenu = false
                                val monthLabel = DateUtils.formatMonthYear(currentYearMonth)
                                val transactions = viewModel.monthlyTransactions.value
                                com.smartbudget.ui.util.CsvExporter.exportAndShare(context, transactions, monthLabel)
                            },
                            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                showOverflowMenu = false
                                onSettings()
                            },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) }
                        )
                    }
                }

                // Line 2: Account selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .offset(y = (-8).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showAccountMenu = true }) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
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
                            modifier = Modifier.size(18.dp)
                        )
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.balance_at, DateUtils.formatDate(selectedDate)),
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

            // Daily transactions header + sort chips
            item {
                Text(
                    text = stringResource(R.string.transactions_for, DateUtils.formatDate(selectedDate)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (dailyTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_transaction),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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
                            val bgColor = when (direction) {
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(icon, contentDescription = null, tint = bgColor)
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
                            text = "$amountPrefix${CurrencyFormatter.format(txn.amount)}",
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
                            val id = txn.id
                            selectedTransaction = null
                            onEditTransaction(id)
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
