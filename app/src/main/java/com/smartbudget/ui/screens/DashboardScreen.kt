package com.smartbudget.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartbudget.R
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.toComposeColor
import com.smartbudget.ui.util.DateUtils
import com.smartbudget.ui.viewmodel.MainViewModel
import com.smartbudget.ui.viewmodel.MonthSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsStateWithLifecycle()
    val monthSummary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val categoryBudgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Month header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.previous_month))
                    }
                    Text(
                        text = DateUtils.formatMonthYear(currentYearMonth),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.navigateMonth(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.next_month))
                    }
                }
            }

            // Pie chart - expenses by category
            item {
                ExpenseByCategoryChart(
                    transactions = monthlyTransactions,
                    totalExpenses = monthSummary.totalExpenses
                )
            }

            // Budget tracking by category
            if (categoryBudgets.isNotEmpty()) {
                item {
                    CategoryBudgetTrackingCard(
                        categoryBudgets = categoryBudgets,
                        transactions = monthlyTransactions
                    )
                }
            }

            // Budget usage card
            item {
                BudgetUsageCard(summary = monthSummary)
            }

            // Income vs Expenses bar
            item {
                IncomeVsExpensesCard(summary = monthSummary)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ExpenseByCategoryChart(
    transactions: List<TransactionWithCategory>,
    totalExpenses: Double
) {
    val expensesByCategory = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryName ?: "Autre" }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val categoryColors = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .associate { (it.categoryName ?: "Autre") to (it.categoryColor?.toComposeColor() ?: Color.Gray) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.expenses_by_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (expensesByCategory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_expense_this_month),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Donut chart
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 40f
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        var startAngle = -90f

                        expensesByCategory.forEach { (category, amount) ->
                            val sweep = (amount / totalExpenses * 360).toFloat()
                            val color = categoryColors[category] ?: Color.Gray

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = CurrencyFormatter.format(totalExpenses),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                expensesByCategory.forEach { (category, amount) ->
                    val color = categoryColors[category] ?: Color.Gray
                    val percent = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(1.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = color)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${String.format("%.1f", percent)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CurrencyFormatter.format(amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetUsageCard(summary: MonthSummary) {
    if (!summary.hasBudget) return

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.budget_usage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val progress = (summary.budgetUsedPercent / 100.0).coerceIn(0.0, 1.0).toFloat()
            val progressColor = when {
                summary.budgetUsedPercent > 100 -> WarningRed
                summary.budgetUsedPercent > 80 -> WarningOrange
                else -> IncomeGreen
            }

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20f
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background arc
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        color = progressColor,
                        startAngle = 135f,
                        sweepAngle = 270f * progress,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.0f", summary.budgetUsedPercent)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = stringResource(R.string.budget_used),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = CurrencyFormatter.format(summary.totalExpenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Text(
                        text = stringResource(R.string.expenses),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = CurrencyFormatter.format(summary.budgetAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.budget),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeVsExpensesCard(summary: MonthSummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.income_vs_expenses),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val maxAmount = maxOf(summary.totalIncome, summary.totalExpenses, 1.0)

            // Income bar
            BarRow(
                label = stringResource(R.string.incomes),
                amount = summary.totalIncome,
                maxAmount = maxAmount,
                color = IncomeGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Expense bar
            BarRow(
                label = stringResource(R.string.expenses),
                amount = summary.totalExpenses,
                maxAmount = maxAmount,
                color = ExpenseRed
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = CurrencyFormatter.formatSigned(summary.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.balance >= 0) IncomeGreen else ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun BarRow(
    label: String,
    amount: Double,
    maxAmount: Double,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (amount / maxAmount).toFloat().coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = color,
            trackColor = color.copy(alpha = 0.1f),
        )
    }
}

@Composable
private fun CategoryBudgetTrackingCard(
    categoryBudgets: List<com.smartbudget.data.entity.Budget>,
    transactions: List<TransactionWithCategory>
) {
    val expensesByCategory = transactions
        .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
        .groupBy { it.categoryId!! }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    val categoryInfo = transactions
        .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
        .associate {
            it.categoryId!! to Pair(
                it.categoryName ?: "Autre",
                it.categoryColor?.toComposeColor() ?: Color.Gray
            )
        }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.budget_tracking),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.budget_vs_actual),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            categoryBudgets.forEach { budget ->
                val catId = budget.categoryId ?: return@forEach
                val spent = expensesByCategory[catId] ?: 0.0
                val info = categoryInfo[catId]
                val catName = info?.first ?: "Catégorie"
                val catColor = info?.second ?: Color.Gray
                val progress = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1.5f) else 0f
                val remaining = budget.amount - spent
                val isOverBudget = spent > budget.amount

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = catName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${CurrencyFormatter.format(spent)} / ${CurrencyFormatter.format(budget.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = progress.coerceAtMost(1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = when {
                            isOverBudget -> ExpenseRed
                            progress > 0.8f -> WarningOrange
                            else -> catColor
                        },
                        trackColor = catColor.copy(alpha = 0.1f),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isOverBudget)
                            stringResource(R.string.budget_exceeded_by, CurrencyFormatter.format(-remaining))
                        else
                            stringResource(R.string.budget_remaining, CurrencyFormatter.format(remaining)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) ExpenseRed else IncomeGreen
                    )
                }
            }
        }
    }
}
