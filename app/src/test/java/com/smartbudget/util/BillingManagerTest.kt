package com.smartbudget.util

import com.smartbudget.billing.BillingManager
import org.junit.Assert.*
import org.junit.Test

class BillingManagerTest {

    @Test
    fun `product IDs are correct`() {
        assertEquals("moneyone_pro_monthly", BillingManager.PRODUCT_ID_MONTHLY)
        assertEquals("moneyone_pro_annual", BillingManager.PRODUCT_ID_ANNUAL)
    }

    @Test
    fun `monthly and annual product IDs are different`() {
        assertNotEquals(BillingManager.PRODUCT_ID_MONTHLY, BillingManager.PRODUCT_ID_ANNUAL)
    }
}
