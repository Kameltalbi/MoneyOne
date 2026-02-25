package com.smartbudget.ui.screens

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
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.DateUtils
import com.smartbudget.ui.util.IconMapper
import com.smartbudget.ui.util.toComposeColor
import com.smartbudget.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBudgetsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val categoryBudgetAmounts by viewModel.categoryBudgetAmounts.collectAsStateWithLifecycle()
    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
    val savedCatBudgets = remember { mutableStateMapOf<Long, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.budget_per_category), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
            // Month selector
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateBudgetMonth(-1) }) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.previous_month))
                        }
                        Text(
                            text = DateUtils.formatMonthYear(selectedYearMonth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.navigateBudgetMonth(1) }) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.next_month))
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.budget_per_category_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category budget items
            items(expenseCategories, key = { it.id }) { category ->
                val catColor = category.color.toComposeColor()
                val budgetAmount = categoryBudgetAmounts[category.id] ?: ""
                val isSaved = savedCatBudgets[category.id] == true

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconMapper.getIcon(category.icon),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = budgetAmount,
                                onValueChange = {
                                    viewModel.updateCategoryBudgetAmount(category.id, it)
                                    savedCatBudgets[category.id] = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                singleLine = true,
                                placeholder = { Text(stringResource(R.string.budget_in, CurrencyFormatter.getCurrencySymbol())) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = {
                                viewModel.saveCategoryBudget(
                                    categoryId = category.id,
                                    isPro = true,
                                    onSuccess = {
                                        savedCatBudgets[category.id] = true
                                    }
                                )
                            },
                            enabled = budgetAmount.isNotBlank() &&
                                    (budgetAmount.toDoubleOrNull() ?: 0.0) > 0,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isSaved) IncomeGreen else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                if (isSaved) Icons.Filled.Check else Icons.Filled.Save,
                                contentDescription = stringResource(R.string.save),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (expenseCategories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_expense_category),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
