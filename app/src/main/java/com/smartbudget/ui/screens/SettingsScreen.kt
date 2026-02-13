package com.smartbudget.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbudget.R
import com.smartbudget.SmartBudgetApp
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.DateUtils
import com.smartbudget.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateCategories: () -> Unit,
    onNavigateCategoryBudgets: () -> Unit,
    onNavigateCurrency: () -> Unit,
    onNavigateAccounts: () -> Unit,
    onNavigateProUpgrade: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as SmartBudgetApp
    val isPro by app.billingManager.isPro.collectAsState()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val globalBudgetAmount by viewModel.globalBudgetAmount.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val selectedCurrencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    var budgetSaved by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var balanceInput by remember { mutableStateOf("") }
    var balanceAdjusted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadCurrentBalance() }

    val currentCurrency = com.smartbudget.data.CurrencyData.getByCode(selectedCurrencyCode)
    val expenseCount = categories.count { it.type == com.smartbudget.data.entity.TransactionType.EXPENSE }
    val incomeCount = categories.count { it.type == com.smartbudget.data.entity.TransactionType.INCOME }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            // Pro upgrade
            if (!isPro) {
                item {
                    Card(
                        onClick = onNavigateProUpgrade,
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.upgrade_to_pro),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.annual_plan_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Accounts
            item {
                SettingsNavItem(
                    icon = Icons.Filled.AccountBalance,
                    title = stringResource(R.string.accounts),
                    subtitle = if (isPro) stringResource(R.string.accounts_count, accounts.size) + " / 5"
                               else stringResource(R.string.free_account_limit),
                    onClick = if (isPro) onNavigateAccounts else onNavigateProUpgrade
                )
            }

            // Currency
            item {
                SettingsNavItem(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.currency),
                    subtitle = "${currentCurrency?.flag ?: ""} ${currentCurrency?.name ?: selectedCurrencyCode} (${currentCurrency?.symbol ?: ""})",
                    onClick = onNavigateCurrency
                )
            }

            // Theme color picker
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.theme_color),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(availableThemeColors) { tc ->
                                val isSelected = tc.name == themeColor
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(tc.primary, CircleShape)
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        )
                                        .clickable {
                                            if (isPro) viewModel.setThemeColor(tc.name)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (!isPro) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.pro_required_desc) + " ⭐",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Google Drive backup
            item {
                val driveManager = remember { com.smartbudget.backup.DriveBackupManager(context) }
                val scope = rememberCoroutineScope()
                var isBackingUp by remember { mutableStateOf(false) }
                var isRestoring by remember { mutableStateOf(false) }
                var backupMessage by remember { mutableStateOf<String?>(null) }
                val isSignedIn = remember { mutableStateOf(driveManager.isSignedIn()) }
                val accountEmail = remember { mutableStateOf(driveManager.getAccountEmail()) }
                val lastBackupTime = remember {
                    val prefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
                    mutableStateOf(prefs.getLong("last_backup_time", 0L))
                }

                val signInLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                        isSignedIn.value = true
                        accountEmail.value = driveManager.getAccountEmail()
                    } catch (_: Exception) {
                        backupMessage = context.getString(R.string.backup_sign_in_failed)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Cloud,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.backup_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!isSignedIn.value) {
                            OutlinedButton(
                                onClick = { signInLauncher.launch(driveManager.getSignInIntent()) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.backup_sign_in))
                            }
                        } else {
                            Text(
                                text = accountEmail.value ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (lastBackupTime.value > 0) {
                                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(lastBackupTime.value))
                                Text(
                                    text = stringResource(R.string.backup_last, dateStr),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        isBackingUp = true
                                        backupMessage = null
                                        scope.launch {
                                            // Checkpoint DB before backup
                                            (context.applicationContext as SmartBudgetApp).getDb().close()
                                            val result = driveManager.backup()
                                            // Reopen DB
                                            (context.applicationContext as SmartBudgetApp).reopenDatabase()
                                            result.onSuccess {
                                                backupMessage = context.getString(R.string.backup_success)
                                                lastBackupTime.value = System.currentTimeMillis()
                                            }.onFailure {
                                                backupMessage = context.getString(R.string.backup_error) + ": ${it.message}"
                                            }
                                            isBackingUp = false
                                        }
                                    },
                                    enabled = !isBackingUp && !isRestoring,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isBackingUp) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.backup_save))
                                }
                                OutlinedButton(
                                    onClick = {
                                        isRestoring = true
                                        backupMessage = null
                                        scope.launch {
                                            (context.applicationContext as SmartBudgetApp).getDb().close()
                                            val result = driveManager.restore()
                                            (context.applicationContext as SmartBudgetApp).reopenDatabase()
                                            result.onSuccess {
                                                backupMessage = context.getString(R.string.backup_restored)
                                            }.onFailure {
                                                backupMessage = context.getString(R.string.backup_restore_error) + ": ${it.message}"
                                            }
                                            isRestoring = false
                                        }
                                    },
                                    enabled = !isBackingUp && !isRestoring,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isRestoring) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.backup_restore))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(onClick = {
                                scope.launch {
                                    driveManager.signOut()
                                    isSignedIn.value = false
                                    accountEmail.value = null
                                }
                            }) {
                                Text(stringResource(R.string.backup_sign_out), style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        backupMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (msg.contains("✓")) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Budget global
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.monthly_global_budget),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.navigateBudgetMonth(-1) }) {
                                Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.previous_month))
                            }
                            Text(
                                text = DateUtils.formatMonthYear(selectedYearMonth),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = { viewModel.navigateBudgetMonth(1) }) {
                                Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.next_month))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = globalBudgetAmount,
                            onValueChange = {
                                viewModel.updateGlobalBudgetAmount(it)
                                budgetSaved = false
                            },
                            label = { Text(stringResource(R.string.global_budget_label, CurrencyFormatter.getCurrencySymbol())) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.AttachMoney, contentDescription = null)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.saveGlobalBudget { budgetSaved = true }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = globalBudgetAmount.isNotBlank() &&
                                    (globalBudgetAmount.toDoubleOrNull() ?: 0.0) > 0
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (budgetSaved) stringResource(R.string.saved) else stringResource(R.string.save_budget))
                        }
                    }
                }
            }

            // Category budgets
            item {
                SettingsNavItem(
                    icon = Icons.Filled.PieChart,
                    title = stringResource(R.string.category_budgets),
                    subtitle = stringResource(R.string.category_budgets_desc),
                    onClick = onNavigateCategoryBudgets
                )
            }

            // Categories
            item {
                SettingsNavItem(
                    icon = Icons.Filled.Label,
                    title = stringResource(R.string.categories) + if (!isPro) " ⭐ Pro" else "",
                    subtitle = "${stringResource(R.string.expenses_count, expenseCount)} · ${stringResource(R.string.incomes_count, incomeCount)}",
                    onClick = if (isPro) onNavigateCategories else onNavigateProUpgrade
                )
            }

            // Language
            item {
                val langLabel = when (selectedLanguage) {
                    "fr" -> stringResource(R.string.language_french)
                    "en" -> stringResource(R.string.language_english)
                    "ar" -> stringResource(R.string.language_arabic)
                    else -> stringResource(R.string.language_french)
                }
                SettingsNavItem(
                    icon = Icons.Filled.Translate,
                    title = stringResource(R.string.language),
                    subtitle = langLabel,
                    onClick = { showLanguageDialog = true }
                )
            }

            // Adjust balance (Pro only)
            if (isPro) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.adjust_balance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentBalance >= 0)
                                    IncomeGreen.copy(alpha = 0.08f)
                                else
                                    ExpenseRed.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.current_balance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatSigned(currentBalance),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentBalance >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = balanceInput,
                            onValueChange = {
                                balanceInput = it.filter { c -> c.isDigit() || c == '.' || c == ',' || c == '-' }.replace(',', '.')
                                balanceAdjusted = false
                            },
                            label = { Text(stringResource(R.string.new_balance)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.AttachMoney, contentDescription = null)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val newBal = balanceInput.toDoubleOrNull() ?: return@Button
                                viewModel.adjustBalance(newBal) {
                                    balanceAdjusted = true
                                    balanceInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = balanceInput.isNotBlank() && balanceInput.toDoubleOrNull() != null
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (balanceAdjusted) stringResource(R.string.balance_adjusted)
                                else stringResource(R.string.adjust)
                            )
                        }
                    }
                }
            }
            } // end isPro for adjust balance

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Language dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.choose_language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "fr" to stringResource(R.string.language_french),
                        "en" to stringResource(R.string.language_english),
                        "ar" to stringResource(R.string.language_arabic)
                    ).forEach { (code, label) ->
                        val isSelected = selectedLanguage == code || (selectedLanguage.isEmpty() && code == "fr")
                        Surface(
                            onClick = {
                                viewModel.setLanguage(code)
                                showLanguageDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}