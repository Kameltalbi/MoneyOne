package com.smartbudget.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbudget.R
import com.smartbudget.data.CurrencyData
import com.smartbudget.data.entity.Account
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Account?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Account?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.accounts), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (accounts.size < 5) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.accounts_count, accounts.size) + " / 5",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(accounts, key = { it.id }) { account ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            val currencyLabel = if (account.currency.isNotBlank()) {
                                val ci = CurrencyData.getByCode(account.currency)
                                "${ci?.flag ?: ""} ${ci?.symbol ?: account.currency}"
                            } else null
                            Text(
                                text = listOfNotNull(
                                    if (account.isDefault) stringResource(R.string.default_account) else null,
                                    currencyLabel
                                ).joinToString(" · ").ifEmpty { "" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (!account.isDefault) {
                            IconButton(onClick = { viewModel.setDefaultAccount(account) }) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = stringResource(R.string.set_as_default),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = stringResource(R.string.default_account),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        IconButton(onClick = { showRenameDialog = account }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (!account.isDefault && accounts.size > 1) {
                            IconButton(onClick = { showDeleteConfirm = account }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = ExpenseRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // Add account dialog
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var selectedCurrency by remember { mutableStateOf("") }
        var showCurrencyPicker by remember { mutableStateOf(false) }
        val currencyDisplay = if (selectedCurrency.isBlank()) stringResource(R.string.default_currency_label)
            else CurrencyData.getByCode(selectedCurrency)?.let { "${it.flag} ${it.code}" } ?: selectedCurrency
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.new_account)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.account_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedButton(
                        onClick = { showCurrencyPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currencyDisplay)
                    }
                    DropdownMenu(
                        expanded = showCurrencyPicker,
                        onDismissRequest = { showCurrencyPicker = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.default_currency_label)) },
                            onClick = { selectedCurrency = ""; showCurrencyPicker = false }
                        )
                        CurrencyData.allCurrencies.forEach { ci ->
                            DropdownMenuItem(
                                text = { Text("${ci.flag} ${ci.code} - ${ci.name}") },
                                onClick = { selectedCurrency = ci.code; showCurrencyPicker = false }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addAccount(
                            name = newName,
                            currency = selectedCurrency,
                            onSuccess = { showAddDialog = false },
                            onError = { errorMessage = it }
                        )
                    },
                    enabled = newName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.create))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Rename dialog
    showRenameDialog?.let { account ->
        var newName by remember { mutableStateOf(account.name) }
        var selectedCurrency by remember { mutableStateOf(account.currency) }
        var showCurrencyPicker by remember { mutableStateOf(false) }
        val currencyDisplay = if (selectedCurrency.isBlank()) stringResource(R.string.default_currency_label)
            else CurrencyData.getByCode(selectedCurrency)?.let { "${it.flag} ${it.code}" } ?: selectedCurrency
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text(stringResource(R.string.rename_account)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.account_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedButton(
                        onClick = { showCurrencyPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currencyDisplay)
                    }
                    DropdownMenu(
                        expanded = showCurrencyPicker,
                        onDismissRequest = { showCurrencyPicker = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.default_currency_label)) },
                            onClick = { selectedCurrency = ""; showCurrencyPicker = false }
                        )
                        CurrencyData.allCurrencies.forEach { ci ->
                            DropdownMenuItem(
                                text = { Text("${ci.flag} ${ci.code} - ${ci.name}") },
                                onClick = { selectedCurrency = ci.code; showCurrencyPicker = false }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameAccount(account.copy(currency = selectedCurrency), newName) {
                            showRenameDialog = null
                        }
                    },
                    enabled = newName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation
    showDeleteConfirm?.let { account ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(stringResource(R.string.delete_account)) },
            text = { Text(stringResource(R.string.delete_account_confirm, account.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(account) { errorMessage = it }
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

    // Error snackbar
    errorMessage?.let { error ->
        val message = when (error) {
            "max_accounts" -> stringResource(R.string.max_accounts_reached)
            "cannot_delete_default" -> stringResource(R.string.cannot_delete_default)
            "last_account" -> stringResource(R.string.last_account)
            else -> error
        }
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("⚠️") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}
