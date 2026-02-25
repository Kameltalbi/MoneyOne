package com.smartbudget.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.sms.ParsedTransaction
import com.smartbudget.ui.theme.ExpenseRed
import com.smartbudget.ui.theme.IncomeGreen
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsImportScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as SmartBudgetApp
    val smsImportService = app.smsImportService
    val scope = rememberCoroutineScope()
    
    val currentAccount by viewModel.currentAccount.collectAsStateWithLifecycle()
    val userManager = remember { com.smartbudget.data.UserManager(context) }
    val userId = userManager.getCurrentUserId()
    
    var parsedTransactions by remember { mutableStateOf<List<ParsedTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedTransactions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var importSuccess by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Import SMS
            scope.launch {
                isLoading = true
                try {
                    val account = currentAccount ?: return@launch
                    // Get existing transactions (simplified - just pass empty list for now)
                    val existingTransactions = emptyList<com.smartbudget.data.entity.Transaction>()
                    val imported = smsImportService.importBankSms(userId, account.id, 30, existingTransactions)
                    parsedTransactions = imported
                    selectedTransactions = imported.indices.toSet()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Import SMS Bancaires",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (importSuccess) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = IncomeGreen.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = IncomeGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${selectedTransactions.size} transactions importées avec succès !",
                            color = IncomeGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import automatique SMS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "MoneyOne va analyser vos SMS bancaires des 30 derniers jours et détecter automatiquement les transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Import button
            Button(
                onClick = {
                    if (smsImportService.hasPermission()) {
                        permissionLauncher.launch(Manifest.permission.READ_SMS)
                    } else {
                        showPermissionDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && currentAccount != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Message, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoading) "Analyse en cours..." else "Analyser mes SMS")
            }
            
            // Results
            if (parsedTransactions.isNotEmpty()) {
                Text(
                    text = "${parsedTransactions.size} transactions détectées",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(parsedTransactions.withIndex().toList()) { (index, transaction) ->
                        TransactionPreviewCard(
                            transaction = transaction,
                            isSelected = selectedTransactions.contains(index),
                            onToggle = {
                                selectedTransactions = if (selectedTransactions.contains(index)) {
                                    selectedTransactions - index
                                } else {
                                    selectedTransactions + index
                                }
                            }
                        )
                    }
                }
                
                // Import selected button
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val account = currentAccount ?: return@launch
                                selectedTransactions.forEach { index ->
                                    val parsed = parsedTransactions[index]
                                    val transaction = smsImportService.toTransaction(
                                        parsed, userId, account.id
                                    )
                                    app.transactionRepository.insert(transaction)
                                }
                                importSuccess = true
                                parsedTransactions = emptyList()
                                selectedTransactions = emptySet()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedTransactions.isNotEmpty() && !isLoading
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importer ${selectedTransactions.size} transactions")
                }
            }
        }
    }
    
    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = { Icon(Icons.Filled.Message, contentDescription = null) },
            title = { Text("Permission SMS requise") },
            text = {
                Text("MoneyOne a besoin d'accéder à vos SMS pour détecter automatiquement vos transactions bancaires. Vos SMS ne seront jamais partagés.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                ) {
                    Text("Autoriser")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun TransactionPreviewCard(
    transaction: ParsedTransaction,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.format(
                    if (transaction.type == TransactionType.EXPENSE) -transaction.amount 
                    else transaction.amount
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
            )
        }
    }
}
