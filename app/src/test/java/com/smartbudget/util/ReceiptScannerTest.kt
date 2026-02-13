package com.smartbudget.util

import com.smartbudget.ui.util.ReceiptAmountExtractor
import org.junit.Assert.*
import org.junit.Test

class ReceiptScannerTest {

    @Test
    fun `extractAmount finds TOTAL with colon`() {
        val amount = ReceiptAmountExtractor.extractAmount("Articles divers\nTOTAL: 42.50\nMerci")
        assertEquals(42.50, amount!!, 0.01)
    }

    @Test
    fun `extractAmount finds total TTC`() {
        val amount = ReceiptAmountExtractor.extractAmount("Sous-total: 10.00\nTVA: 2.00\nTTC 12.00")
        assertEquals(12.00, amount!!, 0.01)
    }

    @Test
    fun `extractAmount finds montant`() {
        val amount = ReceiptAmountExtractor.extractAmount("Montant: 55,99")
        assertEquals(55.99, amount!!, 0.01)
    }

    @Test
    fun `extractAmount finds euro symbol before amount`() {
        val amount = ReceiptAmountExtractor.extractAmount("Prix: €25.00")
        assertEquals(25.00, amount!!, 0.01)
    }

    @Test
    fun `extractAmount finds dollar symbol before amount`() {
        val amount = ReceiptAmountExtractor.extractAmount("Total \$99.99")
        assertEquals(99.99, amount!!, 0.01)
    }

    @Test
    fun `extractAmount finds amount followed by euro`() {
        val amount = ReceiptAmountExtractor.extractAmount("A payer 35,50€")
        assertEquals(35.50, amount!!, 0.01)
    }

    @Test
    fun `extractAmount with comma decimal separator`() {
        val amount = ReceiptAmountExtractor.extractAmount("TOTAL = 123,45")
        assertEquals(123.45, amount!!, 0.01)
    }

    @Test
    fun `extractAmount returns largest standalone amount`() {
        val amount = ReceiptAmountExtractor.extractAmount("Item 1: 5.00\nItem 2: 15.00\nItem 3: 8.50")
        assertEquals(15.00, amount!!, 0.01)
    }

    @Test
    fun `extractAmount returns null for empty text`() {
        assertNull(ReceiptAmountExtractor.extractAmount(""))
    }

    @Test
    fun `extractAmount returns null for text without numbers`() {
        assertNull(ReceiptAmountExtractor.extractAmount("Merci de votre visite"))
    }

    @Test
    fun `extractAmount prefers TOTAL over standalone amounts`() {
        val amount = ReceiptAmountExtractor.extractAmount("Article 99.99\nRemise -10.00\nTOTAL: 89.99")
        assertEquals(89.99, amount!!, 0.01)
    }

    @Test
    fun `extractAmount handles a payer keyword`() {
        val amount = ReceiptAmountExtractor.extractAmount("Net a payer 67,80")
        assertEquals(67.80, amount!!, 0.01)
    }
}
