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

data class ThemeColor(
    val name: String,
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val primaryContainer: Color
)

val availableThemeColors = listOf(
    ThemeColor("emerald", Color(0xFF2E7D32), Color(0xFF4CAF50), Color(0xFF1B5E20), Color(0xFFC8E6C9)),
    ThemeColor("blue", Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF0D47A1), Color(0xFFBBDEFB)),
    ThemeColor("purple", Color(0xFF7B1FA2), Color(0xFFAB47BC), Color(0xFF4A148C), Color(0xFFE1BEE7)),
    ThemeColor("orange", Color(0xFFE65100), Color(0xFFFF9800), Color(0xFFBF360C), Color(0xFFFFE0B2)),
    ThemeColor("red", Color(0xFFC62828), Color(0xFFEF5350), Color(0xFFB71C1C), Color(0xFFFFCDD2)),
    ThemeColor("teal", Color(0xFF00695C), Color(0xFF26A69A), Color(0xFF004D40), Color(0xFFB2DFDB)),
    ThemeColor("indigo", Color(0xFF283593), Color(0xFF5C6BC0), Color(0xFF1A237E), Color(0xFFC5CAE9)),
    ThemeColor("pink", Color(0xFFAD1457), Color(0xFFEC407A), Color(0xFF880E4F), Color(0xFFF8BBD0))
)

private fun buildLightScheme(tc: ThemeColor) = lightColorScheme(
    primary = tc.primary,
    onPrimary = TextOnPrimary,
    primaryContainer = tc.primaryContainer,
    onPrimaryContainer = tc.primaryDark,
    secondary = tc.primary,
    onSecondary = TextOnPrimary,
    secondaryContainer = tc.primaryContainer,
    onSecondaryContainer = tc.primaryDark,
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

private fun buildDarkScheme(tc: ThemeColor) = darkColorScheme(
    primary = tc.primaryLight,
    onPrimary = tc.primaryDark,
    primaryContainer = tc.primaryDark,
    onPrimaryContainer = tc.primaryContainer,
    secondary = tc.primaryLight,
    onSecondary = tc.primaryDark,
    secondaryContainer = tc.primaryDark,
    onSecondaryContainer = tc.primaryContainer,
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
    themeColorName: String = "emerald",
    content: @Composable () -> Unit
) {
    val tc = availableThemeColors.find { it.name == themeColorName } ?: availableThemeColors[0]
    val colorScheme = if (darkTheme) buildDarkScheme(tc) else buildLightScheme(tc)

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
