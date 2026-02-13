package com.smartbudget.ui.util

import androidx.compose.ui.graphics.Color

/**
 * Convert a Long color value (ARGB) to a Compose Color.
 * Room stores colors as Long, but Compose Color() requires ULong.
 */
fun Long.toComposeColor(): Color = Color(this.toInt())
