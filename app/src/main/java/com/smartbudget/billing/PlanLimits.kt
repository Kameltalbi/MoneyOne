package com.smartbudget.billing

object PlanLimits {
    // Free plan limits
    const val FREE_MAX_ACCOUNTS = 2
    const val FREE_MAX_BUDGETS = 3
    const val FREE_MAX_SAVINGS_GOALS = 1
    const val FREE_MAX_CUSTOM_CATEGORIES = 0  // No custom categories for Free
    
    // Pro plan (unlimited)
    const val PRO_MAX_ACCOUNTS = Int.MAX_VALUE
    const val PRO_MAX_BUDGETS = Int.MAX_VALUE
    const val PRO_MAX_SAVINGS_GOALS = Int.MAX_VALUE
    const val PRO_MAX_CUSTOM_CATEGORIES = Int.MAX_VALUE
    
    // Feature flags
    const val FREE_ALLOW_CLOUD_SYNC = false
    const val PRO_ALLOW_CLOUD_SYNC = true
    
    const val FREE_ALLOW_MULTI_CURRENCY = false
    const val PRO_ALLOW_MULTI_CURRENCY = true
    
    const val FREE_ALLOW_RECURRING_EDIT_ALL = false
    const val PRO_ALLOW_RECURRING_EDIT_ALL = true
    
    const val FREE_ALLOW_CUSTOM_CATEGORIES = false
    const val PRO_ALLOW_CUSTOM_CATEGORIES = true
    
    // Free plan: only specific default categories allowed
    // 7 expense categories + 3 income categories
    val FREE_ALLOWED_EXPENSE_CATEGORIES = setOf(
        "Alimentation", "Transport", "Loisirs", "Santé", 
        "Logement", "Shopping", "Autre"
    )
    val FREE_ALLOWED_INCOME_CATEGORIES = setOf(
        "Salaire", "Autre revenu", "Immobilier"
    )
}
