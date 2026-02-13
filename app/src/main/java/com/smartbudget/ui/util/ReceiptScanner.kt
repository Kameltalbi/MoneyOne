package com.smartbudget.ui.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ReceiptResult(
    val amount: Double?,
    val rawText: String
)

object ReceiptAmountExtractor {
    // Regex patterns to find amounts on receipts
    internal val amountPatterns = listOf(
        // TOTAL followed by amount
        Regex("""(?i)(?:total|tot|montant|amount|somme|net|ttc|a\s*payer)\s*[:=]?\s*(\d+[.,]\d{2})"""),
        // Currency symbol followed by amount
        Regex("""[€$£]\s*(\d+[.,]\d{2})"""),
        // Amount followed by currency
        Regex("""(\d+[.,]\d{2})\s*[€$£]"""),
        // Standalone amount (last resort - largest amount)
        Regex("""(\d+[.,]\d{2})""")
    )

    fun extractAmount(text: String): Double? {
        for (pattern in amountPatterns) {
            val matches = pattern.findAll(text).toList()
            if (matches.isNotEmpty()) {
                val amounts = matches.mapNotNull { match ->
                    val amountStr = match.groupValues[1].replace(",", ".")
                    amountStr.toDoubleOrNull()
                }
                if (amounts.isNotEmpty()) {
                    return amounts.max()
                }
            }
        }
        return null
    }
}

object ReceiptScanner {

    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.Builder().build()) }

    // Regex patterns to find amounts on receipts
    private val amountPatterns = listOf(
        // TOTAL followed by amount
        Regex("""(?i)(?:total|tot|montant|amount|somme|net|ttc|a\s*payer)\s*[:=]?\s*(\d+[.,]\d{2})"""),
        // Currency symbol followed by amount
        Regex("""[€$£]\s*(\d+[.,]\d{2})"""),
        // Amount followed by currency
        Regex("""(\d+[.,]\d{2})\s*[€$£]"""),
        // Standalone amount (last resort - largest amount)
        Regex("""(\d+[.,]\d{2})""")
    )

    suspend fun scanReceipt(bitmap: Bitmap): ReceiptResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        val text = suspendCancellableCoroutine<String> { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    cont.resume(result.text)
                }
                .addOnFailureListener {
                    cont.resume("")
                }
        }

        val amount = extractAmount(text)
        return ReceiptResult(amount = amount, rawText = text)
    }

    internal fun extractAmount(text: String): Double? {
        // Try patterns in order of specificity
        for (pattern in amountPatterns) {
            val matches = pattern.findAll(text).toList()
            if (matches.isNotEmpty()) {
                // For the last pattern (generic amounts), take the largest
                val amounts = matches.mapNotNull { match ->
                    val amountStr = match.groupValues[1].replace(",", ".")
                    amountStr.toDoubleOrNull()
                }
                if (amounts.isNotEmpty()) {
                    return amounts.max()
                }
            }
        }
        return null
    }
}
