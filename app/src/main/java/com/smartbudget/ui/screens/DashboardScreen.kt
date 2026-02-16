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
import com.smartbudget.ui.viewmodel.MonthlyTotal
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

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
    val annualData by viewModel.annualData.collectAsStateWithLifecycle()
    val previousMonthSummary by viewModel.previousMonthSummary.collectAsStateWithLifecycle()
    val monthForecast by viewModel.monthForecast.collectAsStateWithLifecycle()

    // Load annual data when year changes
    LaunchedEffect(currentYearMonth.year) {
        viewModel.loadAnnualData(currentYearMonth.year)
    }

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

            // Monthly savings
            item {
                SavingsCard(summary = monthSummary)
            }

            // Month comparison
            item {
                MonthComparisonCard(
                    currentSummary = monthSummary,
                    previousSummary = previousMonthSummary,
                    currentYearMonth = currentYearMonth
                )
            }

            // Annual summary chart
            if (annualData.isNotEmpty()) {
                item {
                    AnnualSummaryChart(
                        year = currentYearMonth.year,
                        data = annualData
                    )
                }
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
    val allExpensesByCategory = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryName ?: "Autre" }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val categoryColors = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .associate { (it.categoryName ?: "Autre") to (it.categoryColor?.toComposeColor() ?: Color.Gray) }

    // Top 5 + regroup the rest as "Autres"
    val expensesByCategory = if (allExpensesByCategory.size > 5) {
        val top5 = allExpensesByCategory.take(5)
        val othersTotal = allExpensesByCategory.drop(5).sumOf { it.second }
        top5 + listOf("Autres" to othersTotal)
    } else {
        allExpensesByCategory
    }
    val chartColors = categoryColors + ("Autres" to Color.Gray)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pie chart (vrai camembert plein)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val padding = 4f
                                var startAngle = -90f

                                expensesByCategory.forEach { (category, amount) ->
                                    val sweep = (amount / totalExpenses * 360).toFloat()
                                    val color = chartColors[category] ?: Color.Gray

                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        topLeft = Offset(padding, padding),
                                        size = Size(size.width - padding * 2, size.height - padding * 2)
                                    )
                                    startAngle += sweep
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CurrencyFormatter.format(totalExpenses),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Legend à droite du camembert
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        expensesByCategory.forEach { (category, amount) ->
                            val color = chartColors[category] ?: Color.Gray
                            val percent = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(color = color)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${String.format("%.0f", percent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
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

@Composable
private fun AnnualSummaryChart(
    year: Int,
    data: List<MonthlyTotal>
) {
    val maxValue = data.maxOfOrNull { maxOf(it.income, it.expenses) } ?: 1.0
    val totalIncome = data.sumOf { it.income }
    val totalExpenses = data.sumOf { it.expenses }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.annual_summary, year),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "↑ ${CurrencyFormatter.format(totalIncome)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = IncomeGreen
                )
                Text(
                    text = "↓ ${CurrencyFormatter.format(totalExpenses)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar chart
            val incomeColor = IncomeGreen
            val expenseColor = ExpenseRed

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height - 20f
                val barGroupWidth = chartWidth / 12f
                val barWidth = barGroupWidth * 0.3f
                val gap = barGroupWidth * 0.05f

                data.forEachIndexed { index, monthly ->
                    val x = index * barGroupWidth

                    val incomeHeight = if (maxValue > 0) (monthly.income / maxValue * chartHeight).toFloat() else 0f
                    drawRect(
                        color = incomeColor,
                        topLeft = Offset(x + gap, chartHeight - incomeHeight),
                        size = Size(barWidth, incomeHeight)
                    )

                    val expenseHeight = if (maxValue > 0) (monthly.expenses / maxValue * chartHeight).toFloat() else 0f
                    drawRect(
                        color = expenseColor,
                        topLeft = Offset(x + gap + barWidth + 1f, chartHeight - expenseHeight),
                        size = Size(barWidth, expenseHeight)
                    )
                }
            }

            // Month labels
            Row(modifier = Modifier.fillMaxWidth()) {
                data.forEach { monthly ->
                    Text(
                        text = monthly.month.month.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(10.dp)) { drawRect(color = incomeColor) }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.income),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Canvas(modifier = Modifier.size(10.dp)) { drawRect(color = expenseColor) }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.expenses),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun MonthComparisonCard(
    currentSummary: MonthSummary,
    previousSummary: MonthSummary,
    currentYearMonth: YearMonth
) {
    val prevMonth = currentYearMonth.minusMonths(1)
    val curLabel = DateUtils.formatMonthYear(currentYearMonth)
    val prevLabel = DateUtils.formatMonthYear(prevMonth)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.month_comparison),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = prevLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(
                    text = curLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Δ",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(0.6f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            ComparisonRow(
                label = stringResource(R.string.income),
                previous = previousSummary.totalIncome,
                current = currentSummary.totalIncome,
                positiveIsGood = true
            )
            Spacer(modifier = Modifier.height(6.dp))
            ComparisonRow(
                label = stringResource(R.string.expenses),
                previous = previousSummary.totalExpenses,
                current = currentSummary.totalExpenses,
                positiveIsGood = false
            )
            Spacer(modifier = Modifier.height(6.dp))
            ComparisonRow(
                label = stringResource(R.string.balance),
                previous = previousSummary.balance,
                current = currentSummary.balance,
                positiveIsGood = true
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    previous: Double,
    current: Double,
    positiveIsGood: Boolean
) {
    val diff = current - previous
    val pctChange = if (previous != 0.0) ((diff / previous) * 100) else if (current > 0) 100.0 else 0.0
    val isPositive = diff >= 0
    val isGood = if (positiveIsGood) isPositive else !isPositive
    val changeColor = if (diff == 0.0) MaterialTheme.colorScheme.onSurfaceVariant
                      else if (isGood) IncomeGreen else ExpenseRed

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = CurrencyFormatter.format(previous),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Text(
            text = CurrencyFormatter.format(current),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Text(
            text = "${if (isPositive) "+" else ""}${pctChange.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = changeColor,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SavingsCard(summary: MonthSummary) {
    val savings = summary.balance
    val savingsColor = if (savings >= 0) IncomeGreen else ExpenseRed
    val savingsRate = if (summary.totalIncome > 0) (savings / summary.totalIncome * 100) else 0.0

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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Économies du mois",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main savings amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Économies totales",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(savings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = savingsColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Savings rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Taux d'épargne",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.1f", savingsRate)}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (savingsRate >= 20) IncomeGreen else if (savingsRate >= 10) WarningOrange else ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = (savingsRate / 100).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (savingsRate >= 20) IncomeGreen else if (savingsRate >= 10) WarningOrange else ExpenseRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Motivational message
            Spacer(modifier = Modifier.height(8.dp))
            val message = when {
                savingsRate >= 20 -> "🎉 Excellent ! Vous épargnez beaucoup !"
                savingsRate >= 10 -> "👍 Bien ! Continuez comme ça !"
                savingsRate >= 0 -> "💡 Essayez d'épargner un peu plus"
                else -> "⚠️ Attention : dépenses supérieures aux revenus"
            }
            Text(
                text = message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
