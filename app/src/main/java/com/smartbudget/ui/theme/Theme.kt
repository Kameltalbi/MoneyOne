package com.smartbudget.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = TextOnPrimary,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = EmeraldGreenDark,
    secondary = Teal,
    onSecondary = TextOnPrimary,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF004D40),
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = TextSecondary,
    error = ExpenseRed,
    onError = TextOnPrimary,
    outline = Color(0xFFBDBDBD)
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreenLight,
    onPrimary = Color(0xFF003300),
    primaryContainer = EmeraldGreenDark,
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = TealLight,
    onSecondary = Color(0xFF003737),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDarkVariant,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = TextSecondaryDark,
    error = Color(0xFFEF5350),
    onError = Color(0xFF690005),
    outline = Color(0xFF616161)
)

@Composable
fun SmartBudgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
