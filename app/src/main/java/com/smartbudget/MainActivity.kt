package com.smartbudget

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartbudget.ui.navigation.SmartBudgetNavigation
import com.smartbudget.ui.theme.SmartBudgetTheme
import com.smartbudget.ui.viewmodel.SettingsViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as SmartBudgetApp
            val userManager = remember { com.smartbudget.data.UserManager(this) }
            val factory = remember { com.smartbudget.ui.viewmodel.ViewModelFactory(app, userManager) }
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            val themeColor by settingsViewModel.themeColor.collectAsState()

            SmartBudgetTheme(themeColorName = themeColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartBudgetNavigation()
                }
            }
        }
    }
}
