package com.smartbudget.ui.screens

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.smartbudget.R
import com.smartbudget.data.CurrencyData
import com.smartbudget.ui.util.CurrencyFormatter

// Steps: 0=Language, 1=Currency, 2-4=Intro pages, 5=Initial balance
private const val STEP_LANGUAGE = 0
private const val STEP_CURRENCY = 1
private const val STEP_INTRO_1 = 2
private const val STEP_INTRO_2 = 3
private const val STEP_INTRO_3 = 4
private const val STEP_BALANCE = 5
private const val TOTAL_STEPS = 6

@Composable
fun OnboardingScreen(
    onFinish: (accountName: String, initialBalance: Double, langCode: String) -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(STEP_LANGUAGE) }
    var selectedLang by remember { mutableStateOf("") }
    var selectedCurrencyCode by remember { mutableStateOf("") }
    var accountNameInput by remember { mutableStateOf("") }
    var balanceInput by remember { mutableStateOf("") }
    var currencySearch by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            when (currentStep) {
                STEP_LANGUAGE -> {
                    // Language selection
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Language,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.choose_your_language),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val languages = listOf(
                        Triple("fr", "Français", "🇫🇷"),
                        Triple("en", "English", "🇬🇧"),
                        Triple("ar", "العربية", "🇸🇦")
                    )

                    languages.forEach { (code, name, flag) ->
                        val isSelected = selectedLang == code
                        Surface(
                            onClick = { selectedLang = code },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                     else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(flag, style = MaterialTheme.typography.headlineMedium)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    PageIndicators(currentStep = currentStep)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            currentStep = STEP_CURRENCY
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedLang.isNotEmpty()
                    ) {
                        Text(
                            stringResource(R.string.next_page),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                STEP_CURRENCY -> {
                    // Currency selection
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.choose_your_currency),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = currencySearch,
                        onValueChange = { currencySearch = it },
                        label = { Text(stringResource(R.string.search)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val uniqueCurrencies = remember {
                        CurrencyData.currencies.distinctBy { it.code }
                    }
                    val filtered = remember(currencySearch) {
                        if (currencySearch.isBlank()) uniqueCurrencies
                        else uniqueCurrencies.filter {
                            it.code.contains(currencySearch, ignoreCase = true) ||
                            it.name.contains(currencySearch, ignoreCase = true) ||
                            it.country.contains(currencySearch, ignoreCase = true) ||
                            it.symbol.contains(currencySearch, ignoreCase = true)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filtered) { currency ->
                            val isSelected = selectedCurrencyCode == currency.code
                            Surface(
                                onClick = { selectedCurrencyCode = currency.code },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(currency.flag, style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${currency.code} - ${currency.symbol}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = currency.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PageIndicators(currentStep = currentStep)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val currency = CurrencyData.currencies.firstOrNull { it.code == selectedCurrencyCode }
                            if (currency != null) {
                                CurrencyFormatter.saveCurrency(context, currency.code, currency.symbol)
                            }
                            currentStep = STEP_INTRO_1
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedCurrencyCode.isNotEmpty()
                    ) {
                        Text(
                            stringResource(R.string.next_page),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                STEP_INTRO_1, STEP_INTRO_2, STEP_INTRO_3 -> {
                    val introData = when (currentStep) {
                        STEP_INTRO_1 -> Triple(Icons.Filled.AccountBalance, R.string.onboarding_title_1, R.string.onboarding_desc_1)
                        STEP_INTRO_2 -> Triple(Icons.Filled.PieChart, R.string.onboarding_title_2, R.string.onboarding_desc_2)
                        else -> Triple(Icons.Filled.TrendingUp, R.string.onboarding_title_3, R.string.onboarding_desc_3)
                    }

                    Spacer(modifier = Modifier.weight(0.15f))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = introData.first,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(introData.second),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(introData.third),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(0.4f))

                    PageIndicators(currentStep = currentStep)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { currentStep++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.next_page),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                STEP_BALANCE -> {
                    Spacer(modifier = Modifier.weight(0.1f))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.initial_balance_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.initial_balance_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = accountNameInput,
                        onValueChange = { accountNameInput = it },
                        label = { Text(stringResource(R.string.account_name_hint)) },
                        leadingIcon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = balanceInput,
                        onValueChange = { balanceInput = it },
                        label = { Text(stringResource(R.string.initial_balance_hint)) },
                        leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.weight(0.3f))

                    PageIndicators(currentStep = currentStep)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val name = accountNameInput.trim().ifEmpty { "Compte principal" }
                            val amount = balanceInput.toDoubleOrNull() ?: 0.0
                            onFinish(name, amount, selectedLang)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = accountNameInput.trim().isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.start),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PageIndicators(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(TOTAL_STEPS) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentStep) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentStep) MaterialTheme.colorScheme.primary
                        else if (index < currentStep) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
            )
        }
    }
}
