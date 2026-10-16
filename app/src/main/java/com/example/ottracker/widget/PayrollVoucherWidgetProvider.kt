package com.example.ottracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.ottracker.MainActivity
import com.example.ottracker.R
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.CutoffHelper
import com.example.ottracker.data.OvertimeRecord
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class PayrollVoucherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateAllWidgets(context)
        }
    }

    companion object {
        private const val TAG = "PayrollVoucherWidget"
        private const val PREFS_NAME = "ot_tracker_prefs"
        private const val KEY_RECORDS = "records_json"
        private const val KEY_SETTINGS = "settings_json"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
                val componentName = ComponentName(context, PayrollVoucherWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (id in appWidgetIds) {
                        updateWidget(context, appWidgetManager, id)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed in updateAllWidgets", e)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

                // 1. Load Settings
                var settings = AppSettings()
                prefs.getString(KEY_SETTINGS, null)?.let { jsonStr ->
                    try {
                        settings = AppSettings.fromJsonObject(JSONObject(jsonStr))
                    } catch (_: Exception) {}
                }

                // 2. Load Records
                val records = mutableListOf<OvertimeRecord>()
                prefs.getString(KEY_RECORDS, null)?.let { jsonStr ->
                    try {
                        val array = JSONArray(jsonStr)
                        for (i in 0 until array.length()) {
                            records.add(OvertimeRecord.fromJsonObject(array.getJSONObject(i)))
                        }
                    } catch (_: Exception) {}
                }

                // 3. Compute Current Cutoff Stats
                val nowMillis = System.currentTimeMillis()
                val currentPeriod = CutoffHelper.getCurrentCutoff(nowMillis, settings)
                val daysLeft = currentPeriod.daysRemaining
                val statusText = if (daysLeft == 1) "[ 1 DAY LEFT ]" else "[ $daysLeft DAYS LEFT ]"
                val periodText = "CUTOFF: ${currentPeriod.title.uppercase(Locale.US)}"

                var cutoffHours = 0.0
                var cutoffPay = 0.0

                for (rec in records) {
                    if (rec.startTimeMillis in currentPeriod.startMillis..currentPeriod.endMillis) {
                        cutoffHours += rec.durationHours
                        cutoffPay += rec.totalPay
                    }
                }

                val payString = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", cutoffPay)}"
                val hoursString = String.format(Locale.US, "%.1f HRS", cutoffHours)

                // 4. Populate RemoteViews
                val views = RemoteViews(context.packageName, R.layout.widget_payroll_voucher).apply {
                    setTextViewText(R.id.widget_voucher_status, statusText)
                    setTextViewText(R.id.widget_voucher_period, periodText)
                    setTextViewText(R.id.widget_voucher_pay, payString)
                    setTextViewText(R.id.widget_voucher_hours, hoursString)
                    setTextViewText(R.id.widget_voucher_btn, "+ LOG OT")

                    // 5. Click Intent to open MainActivity
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        201,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_voucher_root, pendingIntent)
                    setOnClickPendingIntent(R.id.widget_voucher_btn, pendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update voucher widget $appWidgetId", e)
            }
        }
    }
}
