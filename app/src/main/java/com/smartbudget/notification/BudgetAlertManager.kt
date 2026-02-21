package com.smartbudget.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.smartbudget.MainActivity
import com.smartbudget.R
import com.smartbudget.data.entity.Budget
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.data.repository.TransactionRepository
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.DateUtils
import java.time.YearMonth

class BudgetAlertManager(
    private val context: Context,
    private val budgetRepo: BudgetRepository,
    private val transactionRepo: TransactionRepository
) {
    companion object {
        const val CHANNEL_ID = "budget_alerts"
        const val NOTIFICATION_ID_BASE = 1000
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.budget_alert_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.budget_alert_channel_desc)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    suspend fun checkBudgetAlerts(accountId: Long) {
        val userManager = com.smartbudget.data.UserManager(context)
        val userId = userManager.getCurrentUserId()
        val ym = YearMonth.now()
        val ymString = DateUtils.yearMonthString(ym)
        val startDate = ym.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endDate = ym.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val budgets = budgetRepo.getAllBudgetsForMonthDirect(userId, ymString)

        for (budget in budgets) {
            if (budget.amount <= 0) continue

            val spent: Double = if (budget.isGlobal) {
                // Global budget: total expenses
                transactionRepo.getTotalExpensesDirect(userId, accountId, startDate, endDate)
            } else {
                // Category budget
                val catId = budget.categoryId ?: continue
                val spent = transactionRepo.getExpensesByCategoryDirect(userId, accountId, budget.categoryId!!, startDate, endDate)
                spent
            }

            val percent = spent / budget.amount
            val prefs = context.getSharedPreferences("budget_alerts", Context.MODE_PRIVATE)
            val alertKey = "alert_${budget.id}_${ymString}"

            if (percent >= 0.8 && !prefs.getBoolean(alertKey, false)) {
                sendBudgetAlert(budget, spent, percent)
                prefs.edit().putBoolean(alertKey, true).apply()
            }
        }
    }

    private fun sendBudgetAlert(budget: Budget, spent: Double, percent: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percentText = "${(percent * 100).toInt()}%"
        val title = if (budget.isGlobal) {
            context.getString(R.string.budget_alert_global_title, percentText)
        } else {
            context.getString(R.string.budget_alert_category_title, percentText)
        }
        val body = context.getString(
            R.string.budget_alert_body,
            CurrencyFormatter.format(spent),
            CurrencyFormatter.format(budget.amount)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_BASE + budget.id.toInt(),
            notification
        )
    }
}
