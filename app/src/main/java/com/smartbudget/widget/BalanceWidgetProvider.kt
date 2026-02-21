package com.smartbudget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.smartbudget.R
import com.smartbudget.SmartBudgetApp
import com.smartbudget.ui.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class BalanceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, BalanceWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, ids)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as SmartBudgetApp
                val accountRepo = app.accountRepository
                val transactionRepo = app.transactionRepository
                val userManager = com.smartbudget.data.UserManager(context)
                val userId = userManager.getCurrentUserId()

                val account = accountRepo.getDefaultAccount(userId)
                val accountName = account?.name ?: "—"

                // Total balance
                val balance = if (account != null) {
                    transactionRepo.getTotalBalance(userId, account.id)
                } else {
                    transactionRepo.getTotalBalanceAllAccounts(userId)
                }

                // Monthly income/expenses
                val now = YearMonth.now()
                val startOfMonth = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = now.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val monthIncome = if (account != null) {
                    app.getDb().transactionDao().getTotalIncomeDirect(userId, account.id, startOfMonth, endOfMonth)
                } else { 0.0 }

                val monthExpense = if (account != null) {
                    app.getDb().transactionDao().getTotalExpensesDirect(userId, account.id, startOfMonth, endOfMonth)
                } else { 0.0 }

                CurrencyFormatter.init(context)

                val views = RemoteViews(context.packageName, R.layout.widget_balance)
                views.setTextViewText(R.id.widget_account_name, accountName)
                views.setTextViewText(R.id.widget_balance, CurrencyFormatter.format(balance))
                views.setTextViewText(R.id.widget_income, "\u200E↑ ${CurrencyFormatter.format(monthIncome)}")
                views.setTextViewText(R.id.widget_expense, "\u200E↓ ${CurrencyFormatter.format(monthExpense)}")

                // Click opens the app
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (launchIntent != null) {
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        context, 0, launchIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_balance, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_account_name, pendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Silently fail if app not initialized yet
            }
        }
    }

    companion object {
        const val ACTION_UPDATE = "com.smartbudget.WIDGET_UPDATE"

        fun sendUpdateBroadcast(context: Context) {
            val intent = Intent(context, BalanceWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
