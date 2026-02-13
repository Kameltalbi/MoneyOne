package com.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.smartbudget.data.entity.Category
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.IconMapper
import com.smartbudget.ui.util.toComposeColor
import com.smartbudget.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val categoryForm by viewModel.categoryForm.collectAsStateWithLifecycle()

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Category?>(null) }

    val incomeCategories = categories.filter { it.type == TransactionType.INCOME }
    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories), style = MaterialTheme.typography.titleLarge) },
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
            FloatingActionButton(
                onClick = {
                    viewModel.resetCategoryForm()
                    showCategoryDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
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
            // Income categories
            if (incomeCategories.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.incomes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen
                    )
                }
                items(incomeCategories, key = { it.id }) { category ->
                    CategoryListItem(
                        category = category,
                        onEdit = {
                            viewModel.loadCategory(category)
                            showCategoryDialog = true
                        },
                        onDelete = { showDeleteConfirm = category }
                    )
                }
            }

            // Expense categories
            if (expenseCategories.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.expenses),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }
                items(expenseCategories, key = { it.id }) { category ->
                    CategoryListItem(
                        category = category,
                        onEdit = {
                            viewModel.loadCategory(category)
                            showCategoryDialog = true
                        },
                        onDelete = { showDeleteConfirm = category }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // Category dialog
    if (showCategoryDialog) {
        CategoryDialog(
            form = categoryForm,
            onNameChange = { viewModel.updateCategoryName(it) },
            onIconChange = { viewModel.updateCategoryIcon(it) },
            onColorChange = { viewModel.updateCategoryColor(it) },
            onTypeChange = { viewModel.updateCategoryType(it) },
            onSave = {
                viewModel.saveCategory {
                    showCategoryDialog = false
                }
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Delete confirmation
    showDeleteConfirm?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(stringResource(R.string.delete_category)) },
            text = { Text(stringResource(R.string.delete_category_confirm, category.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category)
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

@Composable
private fun CategoryListItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val catColor = category.color.toComposeColor()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
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

            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    form: com.smartbudget.ui.viewmodel.CategoryFormState,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val availableColors = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
        0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
        0xFFFFEB3B, 0xFFFFC107, 0xFFFF9800, 0xFFFF5722,
        0xFF795548, 0xFF607D8B
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (form.isEditing) stringResource(R.string.edit_category) else stringResource(R.string.new_category))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.category_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        val isSelected = form.type == type
                        val label = when (type) {
                            TransactionType.EXPENSE -> stringResource(R.string.expense)
                            TransactionType.INCOME -> stringResource(R.string.income)
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTypeChange(type) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(stringResource(R.string.color), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(availableColors.size) { index ->
                        val color = availableColors[index]
                        val isSelected = form.color == color
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.toComposeColor())
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                        .border(4.dp, color.toComposeColor(), CircleShape)
                                    else Modifier
                                )
                                .clickable { onColorChange(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Text(stringResource(R.string.icon), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                val allIcons = IconMapper.getAllIcons()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allIcons.entries.toList()) { (name, icon) ->
                        val isSelected = form.icon == name
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) form.color.toComposeColor().copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .then(
                                    if (isSelected) Modifier.border(2.dp, form.color.toComposeColor(), RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable { onIconChange(name) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = if (isSelected) form.color.toComposeColor() else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = form.name.isNotBlank(), shape = RoundedCornerShape(12.dp)) {
                Text(if (form.isEditing) stringResource(R.string.edit) else stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
