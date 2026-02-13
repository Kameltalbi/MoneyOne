package com.smartbudget.ui.util

import android.content.Context
import android.content.SharedPreferences
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private const val PREFS_NAME = "smartbudget_prefs"
    private const val KEY_CURRENCY_CODE = "currency_code"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"

    private var symbol: String = "€"
    private var currencyCode: String = "EUR"

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currencyCode = prefs.getString(KEY_CURRENCY_CODE, "EUR") ?: "EUR"
        symbol = prefs.getString(KEY_CURRENCY_SYMBOL, "€") ?: "€"
    }

    fun saveCurrency(context: Context, code: String, sym: String) {
        currencyCode = code
        symbol = sym
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CURRENCY_CODE, code)
            .putString(KEY_CURRENCY_SYMBOL, sym)
            .apply()
    }

    fun getCurrencyCode(): String = currencyCode
    fun getCurrencySymbol(): String = symbol

    private fun getFormatter(): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        return DecimalFormat("#,##0.00", symbols)
    }

    fun format(amount: Double): String {
        return "${getFormatter().format(amount)} $symbol"
    }

    fun formatSigned(amount: Double): String {
        val prefix = if (amount >= 0) "+" else ""
        return "$prefix${getFormatter().format(amount)} $symbol"
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount == 0.0 -> ""
            kotlin.math.abs(amount) >= 1000 -> {
                val k = amount / 1000.0
                String.format(Locale.getDefault(), "%.1fk", k)
            }
            else -> String.format(Locale.getDefault(), "%.0f", amount)
        }
    }
}
