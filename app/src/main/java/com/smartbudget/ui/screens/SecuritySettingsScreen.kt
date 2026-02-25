package com.smartbudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smartbudget.R
import com.smartbudget.SmartBudgetApp
import com.smartbudget.security.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as SmartBudgetApp
    val securityManager = app.securityManager
    val isPro by app.billingManager.isPro.collectAsState()
    
    var pinEnabled by remember { mutableStateOf(securityManager.isPinEnabled()) }
    var biometricEnabled by remember { mutableStateOf(securityManager.isBiometricEnabled()) }
    var showPinDialog by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    
    val biometricAvailable = BiometricHelper.isBiometricAvailable(context)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sécurité",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isPro) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "⭐ Fonctionnalité Pro\nProtégez vos données avec un code PIN ou votre empreinte digitale",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // PIN Code
            Card(
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Code PIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Protégez l'accès à l'application",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pinEnabled,
                            onCheckedChange = { enabled ->
                                if (isPro) {
                                    if (enabled) {
                                        showPinDialog = true
                                    } else {
                                        securityManager.clearPin()
                                        pinEnabled = false
                                    }
                                }
                            },
                            enabled = isPro
                        )
                    }
                    
                    if (pinEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showPinDialog = true }
                        ) {
                            Text("Modifier le code PIN")
                        }
                    }
                }
            }
            
            // Biometric
            if (biometricAvailable) {
                Card(
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Empreinte digitale",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Déverrouiller avec votre empreinte",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { enabled ->
                                if (isPro) {
                                    securityManager.setBiometricEnabled(enabled)
                                    biometricEnabled = enabled
                                }
                            },
                            enabled = isPro
                        )
                    }
                }
            }
        }
    }
    
    // PIN Setup Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPinDialog = false
                newPin = ""
                confirmPin = ""
                pinError = null
            },
            title = { Text("Définir un code PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { 
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                newPin = it
                                pinError = null
                            }
                        },
                        label = { Text("Code PIN (4-6 chiffres)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = pinError != null
                    )
                    
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { 
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                confirmPin = it
                                pinError = null
                            }
                        },
                        label = { Text("Confirmer le code PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = pinError != null
                    )
                    
                    if (pinError != null) {
                        Text(
                            text = pinError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            newPin.length < 4 -> {
                                pinError = "Le code PIN doit contenir au moins 4 chiffres"
                            }
                            newPin != confirmPin -> {
                                pinError = "Les codes PIN ne correspondent pas"
                            }
                            else -> {
                                securityManager.setPinCode(newPin)
                                pinEnabled = true
                                showPinDialog = false
                                newPin = ""
                                confirmPin = ""
                                pinError = null
                            }
                        }
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPinDialog = false
                        newPin = ""
                        confirmPin = ""
                        pinError = null
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}
