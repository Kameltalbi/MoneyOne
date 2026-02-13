package com.smartbudget.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
    const val DASHBOARD = "dashboard"
    const val PRO_UPGRADE = "pro_upgrade"
    const val ONBOARDING = "onboarding"
}

@Composable
fun SmartBudgetNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("moneyone_setup", Context.MODE_PRIVATE)
    val isFirstLaunch = !prefs.getBoolean("onboarding_done", false)
    val startRoute = if (isFirstLaunch) Routes.ONBOARDING else Routes.MAIN

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = { initialBalance ->
                    prefs.edit().putBoolean("onboarding_done", true).apply()
                    prefs.edit().putBoolean("initial_balance_set", true).apply()
                    if (initialBalance > 0) {
                        mainViewModel.setInitialBalance(initialBalance)
                    }
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                onAddTransaction = {
                    transactionViewModel.resetForm(mainViewModel.selectedDate.value)
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onEditTransaction = { transactionId ->
                    transactionViewModel.loadTransaction(transactionId)
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
    }
}
