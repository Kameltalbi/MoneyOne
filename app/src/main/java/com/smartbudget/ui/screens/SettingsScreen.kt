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
    onNavigateProUpgrade: () -> Unit,
    onNavigateSecurity: () -> Unit = {},
    onNavigateSmsImport: () -> Unit = {},
    onNavigateFeatures: () -> Unit = {}
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
                title = { Text(stringResource(R.string.settings), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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

            // Features explanation
            item {
                SettingsNavItem(
                    icon = Icons.Filled.Lightbulb,
                    title = "Découvrir les fonctionnalités",
                    subtitle = "Toutes les fonctionnalités gratuites et Pro",
                    onClick = onNavigateFeatures
                )
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

            // Security (Pro only)
            item {
                SettingsNavItem(
                    icon = Icons.Filled.Lock,
                    title = "Sécurité" + if (!isPro) " ⭐ Pro" else "",
                    subtitle = "Code PIN et empreinte digitale",
                    onClick = if (isPro) onNavigateSecurity else onNavigateProUpgrade
                )
            }

            // SMS Import (Pro only)
            item {
                SettingsNavItem(
                    icon = Icons.Filled.Message,
                    title = "Import SMS" + if (!isPro) " ⭐ Pro" else "",
                    subtitle = "Importer vos transactions bancaires",
                    onClick = if (isPro) onNavigateSmsImport else onNavigateProUpgrade
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

                    // Info about budget purpose
                    Text(
                        text = "💡 Le budget global vous aide à suivre vos dépenses mensuelles. Consultez le tableau de bord pour voir votre progression.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

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
                    subtitle = if (isPro) stringResource(R.string.categories_desc_pro) else stringResource(R.string.categories_desc_free),
                    onClick = if (isPro) onNavigateCategories else onNavigateProUpgrade
                )
            }

            // Category budgets - PRO only
            if (isPro) {
                item {
                    SettingsNavItem(
                        icon = Icons.Filled.PieChart,
                        title = stringResource(R.string.category_budgets),
                        subtitle = stringResource(R.string.category_budgets_desc),
                        onClick = onNavigateCategoryBudgets
                    )
                }
            }

            // Savings goals - PRO only
            if (isPro) {
                item {
                    SettingsNavItem(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = stringResource(R.string.savings_goals),
                        subtitle = stringResource(R.string.savings_goals_desc),
                        onClick = onNavigateSavingsGoals
                    )
                }
            }

            // ── SECTION: Backup ──
            item { SettingsSectionHeader(stringResource(R.string.backup_title)) }

            // Firebase Cloud Sync - PRO only
            if (isPro) {
                item {
                    val app = context.applicationContext as com.smartbudget.SmartBudgetApp
                val firebaseSyncManager = app.firebaseSyncManager
                val firebaseAuthManager = app.firebaseAuthManager
                val userManager = com.smartbudget.data.UserManager(context)
                val scope = rememberCoroutineScope()
                var isSyncing by remember { mutableStateOf(false) }
                var isRestoring by remember { mutableStateOf(false) }
                var syncMessage by remember { mutableStateOf<String?>(null) }
                val isSignedIn = remember { mutableStateOf(firebaseAuthManager.isSignedIn()) }
                val lastSyncTime = remember { mutableStateOf(firebaseSyncManager.getLastSyncTime()) }
                val autoSyncEnabled = remember { mutableStateOf(firebaseSyncManager.isAutoSyncEnabled()) }

                SettingsCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Cloud,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Synchronisation Cloud",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                if (isSignedIn.value) {
                                    Text(
                                        text = if (lastSyncTime.value > 0) {
                                            "Dernière sync: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTime.value))}"
                                        } else {
                                            "Jamais synchronisé"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isSignedIn.value) {
                            // Auto-sync toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        autoSyncEnabled.value = !autoSyncEnabled.value
                                        firebaseSyncManager.setAutoSyncEnabled(autoSyncEnabled.value)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Synchronisation automatique",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = autoSyncEnabled.value,
                                    onCheckedChange = { 
                                        autoSyncEnabled.value = it
                                        firebaseSyncManager.setAutoSyncEnabled(it)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sync now button
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSyncing = true
                                        syncMessage = null
                                        val result = firebaseSyncManager.syncToCloud(userManager.getCurrentUserId())
                                        isSyncing = false
                                        if (result.isSuccess) {
                                            lastSyncTime.value = firebaseSyncManager.getLastSyncTime()
                                            syncMessage = "Synchronisation réussie ✓"
                                        } else {
                                            syncMessage = "Erreur: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                                },
                                enabled = !isSyncing && !isRestoring,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isSyncing) "Synchronisation..." else "Synchroniser maintenant")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Restore button
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isRestoring = true
                                        syncMessage = null
                                        val result = firebaseSyncManager.restoreFromCloud(userManager.getCurrentUserId())
                                        isRestoring = false
                                        if (result.isSuccess) {
                                            syncMessage = "Restauration réussie ✓"
                                        } else {
                                            syncMessage = "Erreur: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                                },
                                enabled = !isSyncing && !isRestoring,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isRestoring) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isRestoring) "Restauration..." else "Restaurer depuis le cloud")
                            }

                            syncMessage?.let { message ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (message.contains("✓")) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Text(
                                text = "Connexion au cloud en cours...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                }
            }

            // Export PDF
            item {
                SettingsNavItem(
                    icon = Icons.Filled.FileDownload,
                    title = stringResource(R.string.export_csv),
                    subtitle = "Exportez vos transactions en PDF",
                    onClick = { /* handled elsewhere */ }
                )
            }

            // Privacy Policy (for all users)
            item {
                SettingsNavItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Politique de confidentialité",
                    subtitle = "Consultez notre politique de confidentialité",
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://sites.google.com/view/moneyone-app/accueil")
                        )
                        context.startActivity(intent)
                    }
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

            // ── SECTION: Personalization (theme color) ──
            item { SettingsSectionHeader(stringResource(R.string.theme_color) + if (!isPro) " ⭐ Pro" else "") }

            item {
                SettingsCard {
                    Column(
                        modifier = if (!isPro) Modifier.clickable { onNavigateProUpgrade() } else Modifier
                    ) {
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
                                            else onNavigateProUpgrade()
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
                                text = "Passez à Pro pour personnaliser les couleurs ⭐",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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