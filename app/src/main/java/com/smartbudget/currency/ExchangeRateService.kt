package com.smartbudget.currency

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Service to fetch and cache exchange rates
 * Uses exchangerate-api.com (free tier: 1500 requests/month)
 */
class ExchangeRateService(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)
    
    companion object {
        private const val API_URL = "https://api.exchangerate-api.com/v4/latest/"
        private const val CACHE_DURATION_HOURS = 24
        private const val KEY_RATES_PREFIX = "rates_"
        private const val KEY_TIMESTAMP_PREFIX = "timestamp_"
    }
    
    /**
     * Get exchange rate from one currency to another
     * @param from Source currency code (e.g., "USD")
     * @param to Target currency code (e.g., "EUR")
     * @return Exchange rate or null if failed
     */
    suspend fun getExchangeRate(from: String, to: String): Double? {
        if (from == to) return 1.0
        
        val rates = getRates(from) ?: return null
        return rates[to]
    }
    
    /**
     * Convert amount from one currency to another
     */
    suspend fun convert(amount: Double, from: String, to: String): Double? {
        val rate = getExchangeRate(from, to) ?: return null
        return amount * rate
    }
    
    /**
     * Get all exchange rates for a base currency
     */
    private suspend fun getRates(baseCurrency: String): Map<String, Double>? {
        // Check cache first
        val cached = getCachedRates(baseCurrency)
        if (cached != null) return cached
        
        // Fetch from API
        return fetchRatesFromApi(baseCurrency)
    }
    
    /**
     * Get cached rates if still valid
     */
    private fun getCachedRates(baseCurrency: String): Map<String, Double>? {
        val timestamp = prefs.getLong(KEY_TIMESTAMP_PREFIX + baseCurrency, 0)
        val now = System.currentTimeMillis()
        
        // Check if cache is still valid (24 hours)
        if (now - timestamp > TimeUnit.HOURS.toMillis(CACHE_DURATION_HOURS.toLong())) {
            return null
        }
        
        val ratesJson = prefs.getString(KEY_RATES_PREFIX + baseCurrency, null) ?: return null
        
        return try {
            val json = JSONObject(ratesJson)
            val rates = mutableMapOf<String, Double>()
            json.keys().forEach { key ->
                rates[key] = json.getDouble(key)
            }
            rates
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Fetch rates from API and cache them
     */
    private suspend fun fetchRatesFromApi(baseCurrency: String): Map<String, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(API_URL + baseCurrency)
                val response = url.readText()
                val json = JSONObject(response)
                val ratesJson = json.getJSONObject("rates")
                
                val rates = mutableMapOf<String, Double>()
                ratesJson.keys().forEach { key ->
                    rates[key] = ratesJson.getDouble(key)
                }
                
                // Cache the rates
                cacheRates(baseCurrency, ratesJson.toString())
                
                rates
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Cache rates in SharedPreferences
     */
    private fun cacheRates(baseCurrency: String, ratesJson: String) {
        prefs.edit()
            .putString(KEY_RATES_PREFIX + baseCurrency, ratesJson)
            .putLong(KEY_TIMESTAMP_PREFIX + baseCurrency, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Clear all cached rates
     */
    fun clearCache() {
        prefs.edit().clear().apply()
    }
}
