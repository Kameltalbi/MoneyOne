package com.smartbudget.sms

import com.smartbudget.data.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.*

data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val date: Long,
    val rawSms: String
)

/**
 * Parser for bank SMS notifications
 * Supports multiple bank formats
 */
object BankSmsParser {
    
    // Common patterns for different banks
    private val patterns = listOf(
        // Pattern 1: "Debit de XXX MAD chez MERCHANT le DD/MM/YYYY"
        BankPattern(
            regex = Regex("(?i)d[eé]bit.*?(\\d+[.,]\\d+).*?(?:chez|at|@)\\s+([A-Za-z0-9\\s]+).*?(\\d{2}[/\\-]\\d{2}[/\\-]\\d{4})?"),
            type = TransactionType.EXPENSE
        ),
        // Pattern 2: "Credit de XXX MAD le DD/MM/YYYY"
        BankPattern(
            regex = Regex("(?i)cr[eé]dit.*?(\\d+[.,]\\d+).*?(\\d{2}[/\\-]\\d{2}[/\\-]\\d{4})?"),
            type = TransactionType.INCOME
        ),
        // Pattern 3: "Achat de XXX MAD chez MERCHANT"
        BankPattern(
            regex = Regex("(?i)achat.*?(\\d+[.,]\\d+).*?(?:chez|at|@)\\s+([A-Za-z0-9\\s]+)"),
            type = TransactionType.EXPENSE
        ),
        // Pattern 4: "Retrait de XXX MAD"
        BankPattern(
            regex = Regex("(?i)retrait.*?(\\d+[.,]\\d+)"),
            type = TransactionType.EXPENSE,
            defaultMerchant = "Retrait ATM"
        ),
        // Pattern 5: "Virement de XXX MAD"
        BankPattern(
            regex = Regex("(?i)virement.*?(\\d+[.,]\\d+)"),
            type = TransactionType.INCOME
        ),
        // Pattern 6: Generic debit pattern
        BankPattern(
            regex = Regex("(?i).*?(\\d+[.,]\\d+)\\s*(?:MAD|EUR|USD|DH).*?(?:chez|at|@)\\s+([A-Za-z0-9\\s]+)"),
            type = TransactionType.EXPENSE
        )
    )
    
    private data class BankPattern(
        val regex: Regex,
        val type: TransactionType,
        val defaultMerchant: String = "Transaction"
    )
    
    /**
     * Parse a bank SMS and extract transaction details
     */
    fun parseSms(smsBody: String, smsDate: Long): ParsedTransaction? {
        for (pattern in patterns) {
            val match = pattern.regex.find(smsBody)
            if (match != null) {
                return extractTransaction(match, pattern, smsBody, smsDate)
            }
        }
        return null
    }
    
    /**
     * Extract transaction details from regex match
     */
    private fun extractTransaction(
        match: MatchResult,
        pattern: BankPattern,
        smsBody: String,
        smsDate: Long
    ): ParsedTransaction? {
        try {
            // Extract amount (group 1)
            val amountStr = match.groupValues.getOrNull(1) ?: return null
            val amount = amountStr.replace(",", ".").toDoubleOrNull() ?: return null
            
            // Extract merchant (group 2 if exists)
            val merchant = match.groupValues.getOrNull(2)?.trim()?.takeIf { it.isNotBlank() }
                ?: pattern.defaultMerchant
            
            // Extract date (group 3 if exists), otherwise use SMS date
            val dateStr = match.groupValues.getOrNull(3)
            val transactionDate = if (dateStr != null && dateStr.isNotBlank()) {
                parseDateString(dateStr) ?: smsDate
            } else {
                smsDate
            }
            
            return ParsedTransaction(
                amount = amount,
                type = pattern.type,
                merchant = cleanMerchantName(merchant),
                date = transactionDate,
                rawSms = smsBody
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Parse date string from SMS (DD/MM/YYYY or DD-MM-YYYY)
     */
    private fun parseDateString(dateStr: String): Long? {
        val formats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        )
        
        for (format in formats) {
            try {
                val date = format.parse(dateStr)
                if (date != null) {
                    return date.time
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }
    
    /**
     * Clean merchant name (remove extra spaces, special chars)
     */
    private fun cleanMerchantName(merchant: String): String {
        return merchant
            .trim()
            .replace(Regex("\\s+"), " ")
            .take(50) // Limit length
    }
    
    /**
     * Check if SMS is likely a bank notification
     */
    fun isBankSms(smsBody: String, sender: String): Boolean {
        val bankKeywords = listOf(
            "debit", "débit", "credit", "crédit", "achat", "retrait",
            "virement", "MAD", "EUR", "USD", "DH", "solde", "compte"
        )
        
        val bodyLower = smsBody.lowercase()
        val hasBankKeyword = bankKeywords.any { bodyLower.contains(it) }
        
        // Check if sender looks like a bank (short code or bank name)
        val isBankSender = sender.length <= 6 || sender.matches(Regex(".*(?:bank|banque|attijariwafa|bmce|cih|bp|sgmb).*", RegexOption.IGNORE_CASE))
        
        return hasBankKeyword && isBankSender
    }
}
