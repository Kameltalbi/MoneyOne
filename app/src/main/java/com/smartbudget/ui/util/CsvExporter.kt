package com.smartbudget.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.TransactionType
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun exportAndShare(
        context: Context,
        transactions: List<TransactionWithCategory>,
        monthLabel: String
    ) {
        val csv = buildCsv(transactions)
        val fileName = "MoneyOne_${monthLabel.replace(" ", "_")}.csv"

        val file = File(context.cacheDir, fileName)
        file.writeText(csv)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MoneyOne - $monthLabel")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export $monthLabel"))
    }

    private fun buildCsv(transactions: List<TransactionWithCategory>): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Nom,Type,Catégorie,Montant,Note,Validé")

        for (t in transactions.sortedBy { it.date }) {
            val date = Instant.ofEpochMilli(t.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(dateFormatter)
            val type = if (t.type == TransactionType.INCOME) "Revenu" else "Dépense"
            val name = escapeCsv(t.name)
            val category = escapeCsv(t.categoryName ?: "")
            val amount = String.format("%.2f", t.amount)
            val note = escapeCsv(t.note)
            val validated = if (t.isValidated) "Oui" else "Non"

            sb.appendLine("$date,$name,$type,$category,$amount,$note,$validated")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
