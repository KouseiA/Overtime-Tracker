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

class CompactGaugeWidgetProvider : AppWidgetProvider() {

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
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateAllWidgets(context)
        }
    }

    companion object {
        private const val TAG = "CompactGaugeWidget"
        private const val PREFS_NAME = "ot_tracker_prefs"
        private const val KEY_RECORDS = "records_json"
        private const val KEY_SETTINGS = "settings_json"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
                val componentName = ComponentName(context, CompactGaugeWidgetProvider::class.java)
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

                var cutoffHours = 0.0
                var cutoffPay = 0.0

                for (rec in records) {
                    if (rec.startTimeMillis in currentPeriod.startMillis..currentPeriod.endMillis) {
                        cutoffHours += rec.durationHours
                        cutoffPay += rec.totalPay
                    }
                }

                // 4. Determine Target Hours & Progress
                val targetHours = when (settings.cutoffFrequency.uppercase(Locale.US)) {
                    "MONTHLY" -> 70.0f
                    "WEEKLY" -> 18.0f
                    else -> 35.0f // Semi-Monthly
                }
                val progress = (cutoffHours / targetHours).toFloat()

                // 5. Draw Dynamic Circular Progress Ring Bitmap
                val gaugeBitmap = GaugeDrawer.drawCircularGauge(
                    sizePx = 280,
                    strokeWidthPx = 24f,
                    progress = progress
                )

                // 6. Format Compact Metrics
                val payString = formatCompactPay(settings.currencySymbol, cutoffPay)
                val hoursString = "${String.format(Locale.US, "%.1f", cutoffHours)} hrs"

                // 7. Populate RemoteViews
                val views = RemoteViews(context.packageName, R.layout.widget_compact_gauge).apply {
                    setImageViewBitmap(R.id.widget_compact_gauge_img, gaugeBitmap)
                    setTextViewText(R.id.widget_compact_pay, payString)
                    setTextViewText(R.id.widget_compact_hours, hoursString)
                    setTextViewText(R.id.widget_compact_badge, "CURRENT CUTOFF")

                    // Click Intent to open MainActivity
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        101,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_compact_root, pendingIntent)
                    setOnClickPendingIntent(R.id.widget_compact_add_btn, pendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update compact widget $appWidgetId", e)
            }
        }

        private fun formatCompactPay(currency: String, amount: Double): String {
            return when {
                amount >= 10000 -> "$currency${String.format(Locale.US, "%.1fk", amount / 1000.0)}"
                amount >= 1000 -> "$currency${String.format(Locale.US, "%.1fk", amount / 1000.0)}"
                else -> "$currency${String.format(Locale.US, "%.0f", amount)}"
            }
        }
    }
}
