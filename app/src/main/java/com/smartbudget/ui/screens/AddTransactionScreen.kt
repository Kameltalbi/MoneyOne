package com.smartbudget.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbudget.R
import com.smartbudget.data.entity.Recurrence
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.DateUtils
import com.smartbudget.ui.util.IconMapper
import com.smartbudget.ui.util.ReceiptScanner
import com.smartbudget.ui.util.toComposeColor
import com.smartbudget.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val filteredCategories by viewModel.filteredCategories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var showRecurringEditDialog by remember { mutableStateOf(false) }

    // Camera photo URI
    val photoFile = remember { File(context.cacheDir, "receipt_photo.jpg") }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            isScanning = true
            scanError = null
            scope.launch {
                try {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        val result = ReceiptScanner.scanReceipt(bitmap)
                        if (result.amount != null) {
                            viewModel.updateAmount(String.format("%.2f", result.amount))
                        } else {
                            scanError = context.getString(R.string.scan_no_amount)
                        }
                    }
                } catch (e: Exception) {
                    scanError = context.getString(R.string.scan_error)
                }
                isScanning = false
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(photoUri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.isEditing) stringResource(R.string.edit_transaction) else stringResource(R.string.new_transaction),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        val isSelected = formState.type == type
                        val label = when (type) {
                            TransactionType.EXPENSE -> stringResource(R.string.expense)
                            TransactionType.INCOME -> stringResource(R.string.income)
                        }
                        val color = when (type) {
                            TransactionType.EXPENSE -> ExpenseRed
                            TransactionType.INCOME -> IncomeGreen
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateType(type) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.15f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }
            }

            // Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text(stringResource(R.string.transaction_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Amount + Scan button
            OutlinedTextField(
                value = formState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.AttachMoney, contentDescription = null)
                },
                trailingIcon = {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                cameraLauncher.launch(photoUri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = stringResource(R.string.scan_receipt),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            // Scan feedback
            scanError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed
                )
            }

            // Category selection
            Text(
                text = stringResource(R.string.category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCategories) { category ->
                    val isSelected = formState.categoryId == category.id
                    val catColor = category.color.toComposeColor()

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    catColor,
                                    RoundedCornerShape(12.dp)
                                )
                                else Modifier
                            )
                            .background(
                                if (isSelected) catColor.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { viewModel.updateCategory(category.id) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconMapper.getIcon(category.icon),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = DateUtils.formatDate(formState.date),
                onValueChange = {},
                label = { Text(stringResource(R.string.date)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Note
            OutlinedTextField(
                value = formState.note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text(stringResource(R.string.note_optional)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                leadingIcon = {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Recurrence
            Text(
                text = stringResource(R.string.recurrence),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            var recurrenceExpanded by remember { mutableStateOf(false) }
            val recurrenceLabel = when (formState.recurrence) {
                Recurrence.NONE -> stringResource(R.string.recurrence_none)
                Recurrence.WEEKLY -> stringResource(R.string.recurrence_weekly)
                Recurrence.MONTHLY -> stringResource(R.string.recurrence_monthly)
                Recurrence.QUARTERLY -> stringResource(R.string.recurrence_quarterly)
                Recurrence.FOUR_MONTHLY -> stringResource(R.string.recurrence_four_monthly)
                Recurrence.SEMI_ANNUAL -> stringResource(R.string.recurrence_semi_annual)
                Recurrence.ANNUAL -> stringResource(R.string.recurrence_annual)
            }

            ExposedDropdownMenuBox(
                expanded = recurrenceExpanded,
                onExpandedChange = { recurrenceExpanded = it }
            ) {
                OutlinedTextField(
                    value = recurrenceLabel,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceExpanded)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded = recurrenceExpanded,
                    onDismissRequest = { recurrenceExpanded = false }
                ) {
                    listOf(
                        Recurrence.NONE to stringResource(R.string.recurrence_none),
                        Recurrence.WEEKLY to stringResource(R.string.recurrence_weekly),
                        Recurrence.MONTHLY to stringResource(R.string.recurrence_monthly),
                        Recurrence.QUARTERLY to stringResource(R.string.recurrence_quarterly),
                        Recurrence.FOUR_MONTHLY to stringResource(R.string.recurrence_four_monthly),
                        Recurrence.SEMI_ANNUAL to stringResource(R.string.recurrence_semi_annual),
                        Recurrence.ANNUAL to stringResource(R.string.recurrence_annual)
                    ).forEach { (recurrence, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.updateRecurrence(recurrence)
                                recurrenceExpanded = false
                            },
                            leadingIcon = {
                                if (formState.recurrence == recurrence) {
                                    Icon(Icons.Filled.Check, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    if (formState.isEditing && formState.isRecurring) {
                        showRecurringEditDialog = true
                    } else {
                        viewModel.saveTransaction(onNavigateBack)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = formState.amount.isNotBlank() &&
                        (formState.amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (formState.isEditing) stringResource(R.string.edit_save) else stringResource(R.string.save_transaction),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Recurring edit choice dialog
    if (showRecurringEditDialog) {
        AlertDialog(
            onDismissRequest = { showRecurringEditDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.edit_recurring_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showRecurringEditDialog = false
                            viewModel.saveTransaction(onNavigateBack)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.edit_recurring_this_only))
                    }
                    Button(
                        onClick = {
                            showRecurringEditDialog = false
                            viewModel.saveTransactionAndFuture(onNavigateBack)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.edit_recurring_this_and_future))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRecurringEditDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtils.toUtcMillis(formState.date)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.updateDate(DateUtils.fromUtcMillis(millis))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
