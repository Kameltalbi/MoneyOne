package com.smartbudget.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
    onNavigateSavingsGoals: () -> Unit,
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

    LaunchedEffect(Unit) {
        viewModel.refreshCurrency()
    }
    
    val currentCurrency = com.smartbudget.data.CurrencyData.getByCode(selectedCurrencyCode)
    val expenseCount = categories.count { it.type == com.smartbudget.data.entity.TransactionType.EXPENSE }
    val incomeCount = categories.count { it.type == com.smartbudget.data.entity.TransactionType.INCOME }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Pro upgrade banner
            if (!isPro) {
                item {
                    Surface(
                        onClick = onNavigateProUpgrade,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.upgrade_to_pro),
                                    style = MaterialTheme.typography.bodyLarge,
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── SECTION: General ──
            item { SettingsSectionHeader(stringResource(R.string.settings)) }

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

            // Language
            item {
                val langLabel = when (selectedLanguage) {
                    "fr" -> stringResource(R.string.language_french)
                    "en" -> stringResource(R.string.language_english)
                    "ar" -> stringResource(R.string.language_arabic)
                    "es" -> stringResource(R.string.language_spanish)
                    "pt" -> stringResource(R.string.language_portuguese)
                    "tr" -> stringResource(R.string.language_turkish)
                    "hi" -> stringResource(R.string.language_hindi)
                    "de" -> stringResource(R.string.language_german)
                    else -> stringResource(R.string.language_french)
                }
                SettingsNavItem(
                    icon = Icons.Filled.Translate,
                    title = stringResource(R.string.language),
                    subtitle = langLabel,
                    onClick = { showLanguageDialog = true }
                )
            }

            // ── SECTION: Budget ──
            item { SettingsSectionHeader(stringResource(R.string.budget)) }

            // Budget global
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.monthly_global_budget),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateBudgetMonth(-1) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.previous_month), modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = DateUtils.formatMonthYear(selectedYearMonth),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = { viewModel.navigateBudgetMonth(1) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.next_month), modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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
                            Icon(Icons.Filled.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { 
                            viewModel.saveGlobalBudget(
                                isPro = isPro,
                                onSuccess = { budgetSaved = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = globalBudgetAmount.isNotBlank() &&
                                (globalBudgetAmount.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (budgetSaved) stringResource(R.string.saved) else stringResource(R.string.save_budget))
                    }
                }
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

            // Category budgets
            item {
                SettingsNavItem(
                    icon = Icons.Filled.PieChart,
                    title = stringResource(R.string.category_budgets),
                    subtitle = stringResource(R.string.category_budgets_desc),
                    onClick = onNavigateCategoryBudgets
                )
            }

            // Savings goals
            item {
                SettingsNavItem(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = stringResource(R.string.savings_goals),
                    subtitle = stringResource(R.string.savings_goals_desc),
                    onClick = onNavigateSavingsGoals
                )
            }

            // ── SECTION: Backup ──
            item { SettingsSectionHeader(stringResource(R.string.backup_title)) }

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

                LaunchedEffect(Unit) {
                    if (!isSignedIn.value || com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context) == null) {
                        val success = driveManager.silentSignIn()
                        if (success) {
                            isSignedIn.value = true
                            accountEmail.value = driveManager.getAccountEmail()
                        }
                    }
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

                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Google Drive",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
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
                                        (context.applicationContext as SmartBudgetApp).getDb().close()
                                        val result = driveManager.backup()
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

            // Export CSV
            item {
                SettingsNavItem(
                    icon = Icons.Filled.FileDownload,
                    title = stringResource(R.string.export_csv),
                    subtitle = "CSV",
                    onClick = { /* handled elsewhere */ }
                )
            }

            // ── SECTION: Adjust balance (Pro only) ──
            if (isPro) {
                item { SettingsSectionHeader(stringResource(R.string.adjust_balance)) }

                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (currentBalance >= 0) IncomeGreen.copy(alpha = 0.08f)
                                    else ExpenseRed.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
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
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (currentBalance >= 0) IncomeGreen else ExpenseRed
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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
                                Icon(Icons.Filled.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

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
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (balanceAdjusted) stringResource(R.string.balance_adjusted)
                                else stringResource(R.string.adjust)
                            )
                        }
                    }
                }
            }

            // ── SECTION: Personalization (theme color at the bottom) ──
            item { SettingsSectionHeader(stringResource(R.string.theme_color)) }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        availableThemeColors.forEach { tc ->
                            val isSelected = tc.name == themeColor
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(tc.primary, CircleShape)
                                    .then(
                                        if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
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
                                        modifier = Modifier.size(16.dp)
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

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Language dialog
    if (showLanguageDialog) {
        val currentLanguage by viewModel.currentLanguage.collectAsState()
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.choose_language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "fr" to stringResource(R.string.language_french),
                        "en" to stringResource(R.string.language_english),
                        "ar" to stringResource(R.string.language_arabic),
                        "es" to stringResource(R.string.language_spanish),
                        "pt" to stringResource(R.string.language_portuguese),
                        "tr" to stringResource(R.string.language_turkish),
                        "hi" to stringResource(R.string.language_hindi)
                    ).forEach { (code, label) ->
                        val isSelected = currentLanguage == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
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

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}