package com.smartbudget.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service to import transactions from bank SMS
 */
class SmsImportService(private val context: Context) {
    
    companion object {
        const val PERMISSION_READ_SMS = Manifest.permission.READ_SMS
    }
    
    /**
     * Check if SMS permission is granted
     */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PERMISSION_READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Import bank SMS from the last N days
     */
    suspend fun importBankSms(
        userId: String,
        accountId: Long,
        daysBack: Int = 30,
        existingTransactions: List<Transaction>
    ): List<ParsedTransaction> = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            return@withContext emptyList()
        }
        
        val importedTransactions = mutableListOf<ParsedTransaction>()
        val cutoffTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        
        val uri: Uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(cutoffTime.toString())
        val sortOrder = "${Telephony.Sms.DATE} DESC"
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            cursor?.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                while (it.moveToNext()) {
                    val sender = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: continue
                    val date = it.getLong(dateIndex)
                    
                    // Check if it's a bank SMS
                    if (!BankSmsParser.isBankSms(body, sender)) {
                        continue
                    }
                    
                    // Parse the SMS
                    val parsed = BankSmsParser.parseSms(body, date) ?: continue
                    
                    // Check if not already imported (avoid duplicates)
                    if (!isDuplicate(parsed, existingTransactions)) {
                        importedTransactions.add(parsed)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        
        importedTransactions
    }
    
    /**
     * Check if transaction is already imported (duplicate detection)
     */
    private fun isDuplicate(
        parsed: ParsedTransaction,
        existingTransactions: List<Transaction>
    ): Boolean {
        // Check if a transaction with same amount, type and date (within 1 hour) exists
        val timeWindow = 60 * 60 * 1000L // 1 hour
        
        return existingTransactions.any { existing ->
            existing.amount == parsed.amount &&
            existing.type == parsed.type &&
            Math.abs(existing.date - parsed.date) < timeWindow
        }
    }
    
    /**
     * Convert ParsedTransaction to Transaction entity
     */
    fun toTransaction(
        parsed: ParsedTransaction,
        userId: String,
        accountId: Long,
        categoryId: Long? = null
    ): Transaction {
        return Transaction(
            name = parsed.merchant,
            amount = parsed.amount,
            type = parsed.type,
            categoryId = categoryId,
            accountId = accountId,
            date = parsed.date,
            note = "Importé depuis SMS",
            userId = userId
        )
    }
}
