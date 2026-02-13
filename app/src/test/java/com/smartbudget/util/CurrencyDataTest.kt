package com.smartbudget.util

import com.smartbudget.data.CurrencyData
import org.junit.Assert.*
import org.junit.Test

class CurrencyDataTest {

    @Test
    fun `currencies list is not empty`() {
        assertTrue(CurrencyData.currencies.isNotEmpty())
    }

    @Test
    fun `allCurrencies has no duplicate codes`() {
        val codes = CurrencyData.allCurrencies.map { it.code }
        assertEquals(codes.size, codes.distinct().size)
    }

    @Test
    fun `getByCode returns EUR`() {
        val eur = CurrencyData.getByCode("EUR")
        assertNotNull(eur)
        assertEquals("€", eur!!.symbol)
    }

    @Test
    fun `getByCode returns USD`() {
        val usd = CurrencyData.getByCode("USD")
        assertNotNull(usd)
        assertEquals("$", usd!!.symbol)
    }

    @Test
    fun `getByCode returns null for unknown code`() {
        val result = CurrencyData.getByCode("XYZ")
        assertNull(result)
    }

    @Test
    fun `search finds by country name`() {
        val results = CurrencyData.search("France")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.country.contains("France", ignoreCase = true) || it.code == "EUR" })
    }

    @Test
    fun `search finds by currency code`() {
        val results = CurrencyData.search("USD")
        assertTrue(results.isNotEmpty())
        assertEquals("USD", results.first().code)
    }

    @Test
    fun `search finds by currency name`() {
        val results = CurrencyData.search("Dirham")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `search is case insensitive`() {
        val upper = CurrencyData.search("EUR")
        val lower = CurrencyData.search("eur")
        assertEquals(upper.size, lower.size)
    }

    @Test
    fun `search with blank query returns all currencies`() {
        val results = CurrencyData.search("")
        assertEquals(CurrencyData.currencies.size, results.size)
    }

    @Test
    fun `allCurrencies contains major currencies`() {
        val codes = CurrencyData.allCurrencies.map { it.code }
        assertTrue("EUR" in codes)
        assertTrue("USD" in codes)
        assertTrue("GBP" in codes)
        assertTrue("JPY" in codes)
        assertTrue("MAD" in codes)
        assertTrue("TND" in codes)
        assertTrue("DZD" in codes)
    }
}
