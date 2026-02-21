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
        
        // Budgets - Pro only
        if (isPro) {
            NavigationBarItem(
                selected = selectedTab == BottomNavTab.BUDGETS,
                onClick = { onTabSelected(BottomNavTab.BUDGETS) },
                icon = { Icon(Icons.Filled.PieChart, contentDescription = null) },
                label = { Text(stringResource(R.string.budgets)) },
                alwaysShowLabel = false
            )
        }
        
        // Accounts - Pro only
        if (isPro) {
            NavigationBarItem(
                selected = selectedTab == BottomNavTab.ACCOUNTS,
                onClick = { onTabSelected(BottomNavTab.ACCOUNTS) },
                icon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) },
                label = { Text(stringResource(R.string.accounts)) },
                alwaysShowLabel = false
            )
        }
        
        // Savings - Pro only
        if (isPro) {
            NavigationBarItem(
                selected = selectedTab == BottomNavTab.SAVINGS,
                onClick = { onTabSelected(BottomNavTab.SAVINGS) },
                icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                label = { Text(stringResource(R.string.savings_goals_title)) },
                alwaysShowLabel = false
            )
        }
        
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
