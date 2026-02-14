package com.smartbudget.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PdfExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun exportAndShare(
        context: Context,
        transactions: List<TransactionWithCategory>,
        monthLabel: String
    ) {
        val file = buildPdf(context, transactions, monthLabel)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MoneyOne - $monthLabel")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export $monthLabel"))
    }

    private fun buildPdf(
        context: Context,
        transactions: List<TransactionWithCategory>,
        monthLabel: String
    ): File {
        val pageWidth = 595  // A4
        val pageHeight = 842
        val margin = 40f
        val document = PdfDocument()

        val sorted = transactions.sortedBy { it.date }

        // Calculate totals
        val totalIncome = sorted.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = sorted.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        // Paints
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1B5E20")
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#388E3C")
            style = Paint.Style.FILL
        }
        val cellPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        val incomePaint = Paint().apply {
            color = Color.parseColor("#2E7D32")
            textSize = 10f
            isAntiAlias = true
        }
        val expensePaint = Paint().apply {
            color = Color.parseColor("#C62828")
            textSize = 10f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 0.5f
        }
        val summaryLabelPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            isAntiAlias = true
        }
        val summaryValuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // Column widths
        val colDate = 70f
        val colName = 130f
        val colCategory = 100f
        val colType = 65f
        val colAmount = 80f
        val colNote = pageWidth - margin * 2 - colDate - colName - colCategory - colType - colAmount
        val rowHeight = 20f
        val headerHeight = 24f

        var pageNumber = 0
        var currentY = 0f
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null

        fun startNewPage() {
            page?.let { document.finishPage(it) }
            pageNumber++
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page!!.canvas
            currentY = margin
        }

        fun ensureSpace(needed: Float) {
            if (currentY + needed > pageHeight - margin) {
                startNewPage()
            }
        }

        fun drawTableHeader() {
            ensureSpace(headerHeight + rowHeight)
            val x = margin
            canvas?.drawRect(x, currentY, pageWidth - margin, currentY + headerHeight, headerBgPaint)
            var cx = x + 4f
            canvas?.drawText("Date", cx, currentY + 16f, headerPaint)
            cx += colDate
            canvas?.drawText("Nom", cx, currentY + 16f, headerPaint)
            cx += colName
            canvas?.drawText("Catégorie", cx, currentY + 16f, headerPaint)
            cx += colCategory
            canvas?.drawText("Type", cx, currentY + 16f, headerPaint)
            cx += colType
            canvas?.drawText("Montant", cx, currentY + 16f, headerPaint)
            cx += colAmount
            canvas?.drawText("Note", cx, currentY + 16f, headerPaint)
            currentY += headerHeight
        }

        // Start first page
        startNewPage()

        // Title
        canvas?.drawText("MoneyOne", margin, currentY + 22f, titlePaint)
        currentY += 30f

        // Month label
        canvas?.drawText(monthLabel, margin, currentY + 14f, subtitlePaint)
        currentY += 28f

        // Summary
        val symbol = CurrencyFormatter.getCurrencySymbol()
        canvas?.drawText("Revenus: ", margin, currentY + 14f, summaryLabelPaint)
        summaryValuePaint.color = Color.parseColor("#2E7D32")
        canvas?.drawText(String.format("%,.2f %s", totalIncome, symbol), margin + 60f, currentY + 14f, summaryValuePaint)

        canvas?.drawText("Dépenses: ", margin + 200f, currentY + 14f, summaryLabelPaint)
        summaryValuePaint.color = Color.parseColor("#C62828")
        canvas?.drawText(String.format("%,.2f %s", totalExpense, symbol), margin + 270f, currentY + 14f, summaryValuePaint)

        canvas?.drawText("Solde: ", margin + 400f, currentY + 14f, summaryLabelPaint)
        summaryValuePaint.color = if (balance >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
        canvas?.drawText(String.format("%,.2f %s", balance, symbol), margin + 440f, currentY + 14f, summaryValuePaint)
        currentY += 30f

        // Table header
        drawTableHeader()

        // Rows
        for ((index, t) in sorted.withIndex()) {
            ensureSpace(rowHeight)

            // If we just started a new page, redraw header
            if (currentY <= margin + 5f) {
                drawTableHeader()
            }

            // Alternate row background
            if (index % 2 == 0) {
                val altBg = Paint().apply {
                    color = Color.parseColor("#F5F5F5")
                    style = Paint.Style.FILL
                }
                canvas?.drawRect(margin, currentY, pageWidth - margin, currentY + rowHeight, altBg)
            }

            val date = Instant.ofEpochMilli(t.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(dateFormatter)
            val type = if (t.type == TransactionType.INCOME) "Revenu" else "Dépense"
            val amountStr = String.format("%,.2f %s", t.amount, symbol)
            val amountPaint = if (t.type == TransactionType.INCOME) incomePaint else expensePaint

            var cx = margin + 4f
            canvas?.drawText(date, cx, currentY + 14f, cellPaint)
            cx += colDate
            canvas?.drawText(truncate(t.name, 22), cx, currentY + 14f, cellPaint)
            cx += colName
            canvas?.drawText(truncate(t.categoryName ?: "", 16), cx, currentY + 14f, cellPaint)
            cx += colCategory
            canvas?.drawText(type, cx, currentY + 14f, cellPaint)
            cx += colType
            canvas?.drawText(amountStr, cx, currentY + 14f, amountPaint)
            cx += colAmount
            canvas?.drawText(truncate(t.note, 12), cx, currentY + 14f, cellPaint)

            // Bottom line
            canvas?.drawLine(margin, currentY + rowHeight, pageWidth - margin, currentY + rowHeight, linePaint)
            currentY += rowHeight
        }

        // Footer
        currentY += 10f
        ensureSpace(20f)
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            isAntiAlias = true
        }
        canvas?.drawText("Généré par MoneyOne — ${sorted.size} transaction(s)", margin, currentY + 10f, footerPaint)

        // Finish
        page?.let { document.finishPage(it) }

        val fileName = "MoneyOne_${monthLabel.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return file
    }

    private fun truncate(text: String, maxLen: Int): String {
        return if (text.length > maxLen) text.take(maxLen - 1) + "…" else text
    }
}
