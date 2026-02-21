package com.smartbudget.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbudget.R
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.entity.Category
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.DateUtils
import com.smartbudget.ui.util.IconMapper
import com.smartbudget.ui.util.toComposeColor
import com.smartbudget.ui.viewmodel.SettingsViewModel
import java.time.format.DateTimeFormatter
import java.time.YearMonth as JavaYearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: SettingsViewModel,
    isPro: Boolean,
    onNavigateProUpgrade: () -> Unit = {}
) {
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val globalBudget by viewModel.currentBudget.collectAsStateWithLifecycle()
    val categoryBudgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val globalBudgetAmount by viewModel.globalBudgetAmount.collectAsStateWithLifecycle()
    
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showGlobalBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(stringResource(R.string.budgets))
                        Text(
                            text = selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateBudgetMonth(-1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null)
                    }
                    IconButton(onClick = { viewModel.navigateBudgetMonth(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (isPro) {
                        showAddBudgetDialog = true
                    } else {
                        onNavigateProUpgrade()
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_budget))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Global budget card
            item {
                GlobalBudgetCard(
                    budget = globalBudget,
                    isPro = isPro,
                    onEdit = { showGlobalBudgetDialog = true },
                    onDelete = { globalBudget?.let { budget -> 
                        viewModel.deleteBudget(budget.id)
                    } },
                    onUpgrade = onNavigateProUpgrade
                )
            }

            // Category budgets header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.category_budgets),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!isPro) {
                        TextButton(onClick = onNavigateProUpgrade) {
                            Text("⭐ Pro")
                        }
                    }
                }
            }

            // Category budgets list
            if (categoryBudgets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                    Icons.Filled.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_category_budgets),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(categoryBudgets) { budget ->
                    val category = categories.find { it.id == budget.categoryId }
                    if (category != null) {
                        CategoryBudgetCard(
                            budget = budget,
                            category = category,
                            onEdit = { selectedCategory = category },
                            onDelete = { viewModel.deleteCategoryBudget(budget.categoryId!!) }
                        )
                    }
                }
            }
        }
    }

    // Add Budget Dialog
    if (showAddBudgetDialog) {
        AddBudgetDialog(
            categories = categories.filter { cat -> 
                categoryBudgets.none { it.categoryId == cat.id }
            },
            onDismiss = { showAddBudgetDialog = false },
            onConfirmGlobal = {
                showAddBudgetDialog = false
                showGlobalBudgetDialog = true
            },
            onConfirmCategory = { category ->
                selectedCategory = category
                showAddBudgetDialog = false
            }
        )
    }

    // Global Budget Dialog
    if (showGlobalBudgetDialog) {
        BudgetAmountDialog(
            title = stringResource(R.string.global_budget),
            currentAmount = globalBudgetAmount,
            onAmountChange = { viewModel.updateGlobalBudgetAmount(it) },
            onDismiss = { 
                showGlobalBudgetDialog = false
                viewModel.updateGlobalBudgetAmount("")
            },
            onConfirm = {
                viewModel.saveGlobalBudget(
                    isPro = isPro,
                    onSuccess = { 
                        showGlobalBudgetDialog = false
                        viewModel.updateGlobalBudgetAmount("")
                    },
                    onError = { error ->
                        errorMessage = error
                    }
                )
            }
        )
    }

    // Category Budget Dialog
    selectedCategory?.let { category ->
        val categoryBudgetAmounts by viewModel.categoryBudgetAmounts.collectAsStateWithLifecycle()
        val currentAmount = categoryBudgetAmounts[category.id] ?: ""
        
        BudgetAmountDialog(
            title = "${stringResource(R.string.budget_for)} ${category.name}",
            currentAmount = currentAmount,
            onAmountChange = { viewModel.updateCategoryBudgetAmount(category.id, it) },
            onDismiss = { selectedCategory = null },
            onConfirm = {
                viewModel.saveCategoryBudget(
                    categoryId = category.id,
                    isPro = isPro,
                    onSuccess = { 
                        selectedCategory = null
                    },
                    onError = { error ->
                        errorMessage = error
                    }
                )
            }
        )
    }
}

@Composable
private fun GlobalBudgetCard(
    budget: Budget?,
    isPro: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.global_budget),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (budget != null) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (budget != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = CurrencyFormatter.format(budget.amount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val progress = 0.65f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(800),
                    label = "progress"
                )
                
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (progress > 0.9f) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${(progress * 100).toInt()}% ${stringResource(R.string.used)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                if (isPro) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.set_global_budget))
                    }
                } else {
                    TextButton(onClick = onUpgrade) {
                        Text("⭐ ${stringResource(R.string.upgrade_to_pro)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    budget: Budget,
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = category.color.toComposeColor()
    val progress = 0.45f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        IconMapper.getIcon(category.icon),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = CurrencyFormatter.format(budget.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (progress > 0.9f) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress > 0.9f) MaterialTheme.colorScheme.error else categoryColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.edit))
                }
            }
        }
    }
}

@Composable
private fun AddBudgetDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirmGlobal: () -> Unit,
    onConfirmCategory: (Category) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_budget)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onConfirmGlobal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.global_budget))
                }
                
                if (categories.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.or_category_budget),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    categories.take(5).forEach { category ->
                        OutlinedButton(
                            onClick = { onConfirmCategory(category) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                IconMapper.getIcon(category.icon),
                                contentDescription = null,
                                tint = category.color.toComposeColor()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.name)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun BudgetAmountDialog(
    title: String,
    currentAmount: String,
    onAmountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = currentAmount,
                onValueChange = onAmountChange,
                label = { Text(stringResource(R.string.amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
