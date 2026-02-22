package com.smartbudget.ui.navigation

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartbudget.ui.screens.*
import com.smartbudget.ui.viewmodel.MainViewModel
import com.smartbudget.ui.viewmodel.SettingsViewModel
import com.smartbudget.ui.viewmodel.TransactionViewModel

object Routes {
    const val MAIN = "main"
    const val ADD_TRANSACTION = "add_transaction"
    const val SETTINGS = "settings"
    const val SETTINGS_CATEGORIES = "settings/categories"
    const val SETTINGS_CATEGORY_BUDGETS = "settings/category_budgets"
    const val SETTINGS_CURRENCY = "settings/currency"
    const val SETTINGS_ACCOUNTS = "settings/accounts"
    const val BUDGETS = "budgets"
    const val DASHBOARD = "dashboard"
    const val SMART_INSIGHTS = "smart_insights"
    const val PRO_UPGRADE = "pro_upgrade"
    const val ONBOARDING = "onboarding"
    const val SAVINGS_GOALS = "savings_goals"
    const val SEARCH = "search"
}

@Composable
fun SmartBudgetNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.smartbudget.SmartBudgetApp
    val userManager = remember { com.smartbudget.data.UserManager(context) }
    val factory = remember { com.smartbudget.ui.viewmodel.ViewModelFactory(app, userManager) }
    
    val mainViewModel: MainViewModel = viewModel(factory = factory)
    val transactionViewModel: TransactionViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val prefs = context.getSharedPreferences("moneyone_setup", Context.MODE_PRIVATE)
    val isFirstLaunch = !prefs.getBoolean("onboarding_done", false)
    val startRoute = if (isFirstLaunch) Routes.ONBOARDING else Routes.MAIN

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = { accountName, initialBalance, langCode ->
                    prefs.edit().putBoolean("onboarding_done", true).apply()
                    prefs.edit().putBoolean("initial_balance_set", true).apply()
                    // Save language preference
                    if (langCode.isNotEmpty()) {
                        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .edit().putString("language", langCode).apply()
                    }
                    // Create account and set initial balance
                    mainViewModel.createFirstAccount(accountName, initialBalance)
                    // Navigate to main screen
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                    // Apply locale after navigation (will restart activity, but onboarding_done is saved)
                    if (langCode.isNotEmpty()) {
                        val localeList = LocaleListCompat.forLanguageTags(langCode)
                        AppCompatDelegate.setApplicationLocales(localeList)
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                onAddTransaction = {
                    val accountId = if (mainViewModel.isConsolidated.value) {
                        null // Will use default account
                    } else {
                        mainViewModel.currentAccount.value?.id
                    }
                    transactionViewModel.resetForm(mainViewModel.selectedDate.value, accountId)
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onEditTransaction = { transactionId ->
                    transactionViewModel.loadTransaction(transactionId)
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onEditRecurringTransaction = { transactionId, mode ->
                    transactionViewModel.loadTransaction(transactionId)
                    transactionViewModel.setRecurringEditMode(
                        com.smartbudget.ui.viewmodel.RecurringEditMode.valueOf(mode)
                    )
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onDashboard = {
                    navController.navigate(Routes.DASHBOARD)
                },
                onProUpgrade = {
                    navController.navigate(Routes.PRO_UPGRADE)
                },
                onSavingsGoals = {
                    navController.navigate(Routes.SAVINGS_GOALS)
                },
                onSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToSmartInsights = {
                    navController.navigate(Routes.SMART_INSIGHTS)
                }
            )
        }

        composable(Routes.ADD_TRANSACTION) {
            AddTransactionScreen(
                viewModel = transactionViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateCategories = { navController.navigate(Routes.SETTINGS_CATEGORIES) },
                onNavigateCategoryBudgets = { navController.navigate(Routes.SETTINGS_CATEGORY_BUDGETS) },
                onNavigateCurrency = { navController.navigate(Routes.SETTINGS_CURRENCY) },
                onNavigateAccounts = { navController.navigate(Routes.SETTINGS_ACCOUNTS) },
                onNavigateSavingsGoals = { navController.navigate(Routes.SAVINGS_GOALS) },
                onNavigateProUpgrade = { navController.navigate(Routes.PRO_UPGRADE) }
            )
        }

        composable(Routes.SETTINGS_CATEGORIES) {
            CategoriesScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_CATEGORY_BUDGETS) {
            CategoryBudgetsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_CURRENCY) {
            CurrencyScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_ACCOUNTS) {
            AccountsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PRO_UPGRADE) {
            ProUpgradeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = mainViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditTransaction = { transactionId ->
                    transactionViewModel.loadTransaction(transactionId)
                    navController.navigate(Routes.ADD_TRANSACTION)
                }
            )
        }

        composable(Routes.BUDGETS) {
            val app = context.applicationContext as com.smartbudget.SmartBudgetApp
            val isPro by app.billingManager.isPro.collectAsState()
            BudgetsScreen(
                viewModel = settingsViewModel,
                isPro = isPro,
                onNavigateProUpgrade = { navController.navigate(Routes.PRO_UPGRADE) }
            )
        }

        composable(Routes.SMART_INSIGHTS) {
            val app = context.applicationContext as com.smartbudget.SmartBudgetApp
            val isPro by app.billingManager.isPro.collectAsState()
            
            if (isPro) {
                val userManager = remember { com.smartbudget.data.UserManager(context) }
                val insightsViewModel: com.smartbudget.ui.viewmodel.SmartInsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.smartbudget.ui.viewmodel.ViewModelFactory(
                        context.applicationContext as android.app.Application,
                        userManager
                    )
                )
                SmartInsightsScreen(
                    viewModel = insightsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.PRO_UPGRADE) {
                        popUpTo(Routes.MAIN)
                    }
                }
            }
        }

        composable(Routes.SAVINGS_GOALS) {
            val app = context.applicationContext as com.smartbudget.SmartBudgetApp
            val isPro by app.billingManager.isPro.collectAsState()
            val goals by mainViewModel.savingsGoals.collectAsState()
            SavingsGoalsScreen(
                goals = goals,
                onAddGoal = { name, target -> mainViewModel.addSavingsGoal(name, target, isPro) },
                onAddAmount = { goalId, amount -> mainViewModel.addAmountToGoal(goalId, amount) },
                onDeleteGoal = { goal -> mainViewModel.deleteSavingsGoal(goal) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
