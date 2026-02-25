package com.smartbudget.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.smartbudget.data.entity.Transaction
import com.smartbudget.data.entity.TransactionType
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * Export transactions to CSV file and return share intent
     */
    fun exportTransactionsToCsv(
        context: Context,
        transactions: List<Transaction>,
        categoryNames: Map<Long, String>,
        accountNames: Map<Long, String>
    ): Intent? {
        try {
            // Create CSV file in cache directory
            val fileName = "MoneyOne_Export_${fileNameFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)
            
            FileWriter(file).use { writer ->
                // Write CSV header
                writer.append("Date,Type,Name,Amount,Category,Account,Note\n")
                
                // Write transactions
                transactions.forEach { transaction ->
                    val date = dateFormat.format(Date(transaction.date))
                    val type = when (transaction.type) {
                        TransactionType.EXPENSE -> "Expense"
                        TransactionType.INCOME -> "Income"
                        TransactionType.TRANSFER -> "Transfer"
                    }
                    val name = escapeCsv(transaction.name)
                    val amount = transaction.amount.toString()
                    val category = transaction.categoryId?.let { categoryNames[it] } ?: ""
                    val account = transaction.accountId?.let { accountNames[it] } ?: ""
                    val note = escapeCsv(transaction.note)
                    
                    writer.append("$date,$type,$name,$amount,$category,$account,$note\n")
                }
            }
            
            // Create share intent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            return Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MoneyOne Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Escape special characters for CSV format
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
