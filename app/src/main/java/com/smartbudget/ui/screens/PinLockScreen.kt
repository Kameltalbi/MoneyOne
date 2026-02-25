package com.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.SmartBudgetApp

@Composable
fun PinLockScreen(
    onUnlocked: () -> Unit,
    onBiometricRequest: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as SmartBudgetApp
    val securityManager = app.securityManager
    
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val biometricEnabled = securityManager.isBiometricEnabled()
    
    LaunchedEffect(enteredPin) {
        if (enteredPin.length >= 4) {
            if (securityManager.verifyPin(enteredPin)) {
                onUnlocked()
            } else {
                isError = true
                kotlinx.coroutines.delay(500)
                enteredPin = ""
                isError = false
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // App Icon/Title
            Text(
                text = "🔒",
                fontSize = 64.sp
            )
            
            Text(
                text = "Entrez votre code PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (isError) MaterialTheme.colorScheme.error
                                else if (index < enteredPin.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .background(
                                if (index < enteredPin.length) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                    )
                }
            }
            
            if (isError) {
                Text(
                    text = "Code PIN incorrect",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Number Pad
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rows 1-3
                for (row in 0..2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (col in 1..3) {
                            val number = row * 3 + col
                            PinButton(
                                text = number.toString(),
                                onClick = {
                                    if (enteredPin.length < 6) {
                                        enteredPin += number
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Row 4: Biometric, 0, Backspace
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Biometric button
                    if (biometricEnabled) {
                        IconButton(
                            onClick = onBiometricRequest,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                Icons.Filled.Fingerprint,
                                contentDescription = "Empreinte digitale",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(72.dp))
                    }
                    
                    // 0
                    PinButton(
                        text = "0",
                        onClick = {
                            if (enteredPin.length < 6) {
                                enteredPin += "0"
                            }
                        }
                    )
                    
                    // Backspace
                    IconButton(
                        onClick = {
                            if (enteredPin.isNotEmpty()) {
                                enteredPin = enteredPin.dropLast(1)
                            }
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            Icons.Filled.Backspace,
                            contentDescription = "Effacer",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PinButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
