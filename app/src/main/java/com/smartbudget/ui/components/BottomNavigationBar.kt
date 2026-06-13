package com.smartbudget.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smartbudget.R

enum class BottomNavTab {
    HOME,
    BUDGETS,
    ACCOUNTS,
    SAVINGS,
    MORE
}

@Composable
fun BottomNavigationBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    isPro: Boolean = true,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        // Home - always visible
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.HOME,
            onClick = { onTabSelected(BottomNavTab.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.home)) },
            alwaysShowLabel = false
        )
        
        // Budgets - FREE during testing
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.BUDGETS,
            onClick = { onTabSelected(BottomNavTab.BUDGETS) },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Budgets") },
            label = { Text("Budgets") },
            alwaysShowLabel = false
        )
        
        // Accounts - FREE during testing
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.ACCOUNTS,
            onClick = { onTabSelected(BottomNavTab.ACCOUNTS) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Accounts") },
            label = { Text("Accounts") },
            alwaysShowLabel = false
        )
        
        // Savings - FREE during testing
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.SAVINGS,
            onClick = { onTabSelected(BottomNavTab.SAVINGS) },
            icon = { Icon(Icons.Default.Savings, contentDescription = "Savings") },
            label = { Text("Savings") },
            alwaysShowLabel = false
        )
        
        // More - always visible
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.MORE,
            onClick = { onTabSelected(BottomNavTab.MORE) },
            icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = null) },
            label = { Text(stringResource(R.string.more)) },
            alwaysShowLabel = false
        )
    }
}
