package com.smartbudget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.SmartBudgetApp
import com.smartbudget.data.UserManager
import com.smartbudget.data.entity.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class SmartInsightsState(
    val monthlyVariation: Double = 0.0,
    val topIncreaseCategory: Pair<String, Double>? = null,
    val endOfMonthProjection: Double = 0.0,
    val disciplineScore: Int = 100,
    val autoAdvice: String = ""
)

class SmartInsightsViewModel(
    application: Application,
    private val userManager: UserManager
) : AndroidViewModel(application) {
    private val app = application as SmartBudgetApp
    private val transactionRepo = app.transactionRepository
    private val budgetRepo = app.budgetRepository
    private val savingsGoalRepo = app.savingsGoalRepository
    private val categoryRepo = app.categoryRepository
    
    private val userId: String
        get() = userManager.getCurrentUserId()
    
    private val _insightsState = MutableStateFlow(SmartInsightsState())
    val insightsState: StateFlow<SmartInsightsState> = _insightsState.asStateFlow()
    
    init {
        calculateInsights()
    }
    
    fun calculateInsights() {
        viewModelScope.launch {
            val currentMonth = YearMonth.now()
            val previousMonth = currentMonth.minusMonths(1)
            
            // Get current and previous month expenses
            val currentExpenses = getMonthExpenses(currentMonth)
            val previousExpenses = getMonthExpenses(previousMonth)
            
            // 1. Monthly variation
            val variation = if (previousExpenses > 0) {
                ((currentExpenses - previousExpenses) / previousExpenses) * 100
            } else 0.0
            
            // 2. Top increase category
            val topCategory = getTopIncreaseCategory(currentMonth, previousMonth)
            
            // 3. End of month projection
            val projection = calculateProjection(currentMonth, currentExpenses)
            
            // 4. Discipline score
            val score = calculateDisciplineScore(currentMonth, variation)
            
            // 5. Auto advice
            val advice = generateAdvice(variation, topCategory, score)
            
            _insightsState.value = SmartInsightsState(
                monthlyVariation = variation,
                topIncreaseCategory = topCategory,
                endOfMonthProjection = projection,
                disciplineScore = score,
                autoAdvice = advice
            )
        }
    }
    
    private suspend fun getMonthExpenses(yearMonth: YearMonth): Double {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val startMillis = startDate.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val endMillis = endDate.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        
        return transactionRepo.getTransactionsByDateRangeDirect(userId, startMillis, endMillis)
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
    
    private suspend fun getTopIncreaseCategory(
        currentMonth: YearMonth,
        previousMonth: YearMonth
    ): Pair<String, Double>? {
        val categories = categoryRepo.getAllCategoriesDirect(userId)
        
        val increases = mutableListOf<Pair<String, Double>>()
        for (category in categories) {
            val currentAmount = getCategoryExpenses(currentMonth, category.id)
            val previousAmount = getCategoryExpenses(previousMonth, category.id)
            
            if (previousAmount > 0) {
                val increase = ((currentAmount - previousAmount) / previousAmount) * 100
                if (increase > 0) {
                    increases.add(category.name to increase)
                }
            }
        }
        
        return increases.maxByOrNull { it.second }
    }
    
    private suspend fun getCategoryExpenses(yearMonth: YearMonth, categoryId: Long): Double {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val startMillis = startDate.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val endMillis = endDate.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        
        return transactionRepo.getTransactionsByDateRangeDirect(userId, startMillis, endMillis)
            .filter { it.type == TransactionType.EXPENSE && it.categoryId == categoryId }
            .sumOf { it.amount }
    }
    
    private fun calculateProjection(yearMonth: YearMonth, currentExpenses: Double): Double {
        val today = LocalDate.now()
        val daysElapsed = today.dayOfMonth
        val totalDays = yearMonth.lengthOfMonth()
        
        return if (daysElapsed > 0) {
            (currentExpenses / daysElapsed) * totalDays
        } else currentExpenses
    }
    
    private suspend fun calculateDisciplineScore(yearMonth: YearMonth, variation: Double): Int {
        var score = 100
        
        // -15 if budget exceeded
        val budgets = budgetRepo.getAllBudgetsForMonthDirect(userId, "${yearMonth.year}-${yearMonth.monthValue.toString().padStart(2, '0')}")
        val currentExpenses = getMonthExpenses(yearMonth)
        val globalBudget = budgets.firstOrNull { it.isGlobal }
        
        if (globalBudget != null && currentExpenses > globalBudget.amount) {
            score -= 15
        }
        
        // -10 if increase > 20%
        if (variation > 20) {
            score -= 10
        }
        
        // +5 if positive savings
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val startMillis = startDate.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val endMillis = endDate.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        
        val income = transactionRepo.getTransactionsByDateRangeDirect(userId, startMillis, endMillis)
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        if (income > currentExpenses) {
            score += 5
        }
        
        return score.coerceIn(0, 100)
    }
    
    private fun generateAdvice(
        variation: Double,
        topCategory: Pair<String, Double>?,
        score: Int
    ): String {
        return when {
            score < 70 -> "Attention : vos dépenses augmentent. Révisez votre budget."
            variation > 20 -> "Hausse importante détectée. Analysez vos dépenses récentes."
            topCategory != null && topCategory.second > 30 -> 
                "La catégorie ${topCategory.first} a fortement augmenté (+${topCategory.second.toInt()}%)."
            score >= 95 -> "Excellent ! Vous maîtrisez parfaitement votre budget."
            else -> "Bon contrôle budgétaire. Continuez ainsi !"
        }
    }
}
