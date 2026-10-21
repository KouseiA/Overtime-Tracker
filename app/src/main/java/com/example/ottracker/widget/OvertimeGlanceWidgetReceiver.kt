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
import com.example.ottracker.data.calculateTotalPay
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * High-performance AppWidgetProvider for the 4x2 Cutoff Terminal Widget (Concept 1).
 * Uses standard Android RemoteViews for 100% instantaneous, deterministic updates
 * without WorkManager queuing or OEM background throttling.
 */
class OvertimeGlanceWidgetReceiver : AppWidgetProvider() {

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
        private const val TAG = "OvertimeCutoffWidget"
        private const val PREFS_NAME = "ot_tracker_prefs"
        private const val KEY_RECORDS = "records_json"
        private const val KEY_SETTINGS = "settings_json"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
                val componentName = ComponentName(context, OvertimeGlanceWidgetReceiver::class.java)
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
                val daysText = if (daysLeft == 1) "1 DAY LEFT" else "$daysLeft DAYS LEFT"
                val subtitleText = "${currentPeriod.title.uppercase(Locale.US)} · $daysText"

                val cutoffRecords = records.filter { it.startTimeMillis in currentPeriod.startMillis..currentPeriod.endMillis }
                val cutoffHours = cutoffRecords.sumOf { it.durationHours }
                val cutoffPay = cutoffRecords.calculateTotalPay()

                val payString = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", cutoffPay)}"
                val hoursRenderedText = "${String.format(Locale.US, "%.1f", cutoffHours)} hrs rendered"

                // 4. Determine Target Hours & Draw Dynamic Tip-Aligned Progress Bar
                val targetHours = if (settings.targetOtHours > 0.0) settings.targetOtHours else 30.0
                val progress = (cutoffHours / targetHours).toFloat().coerceIn(0.0f, 1.0f)
                val targetDisplay = if (targetHours % 1.0 == 0.0) {
                    String.format(Locale.US, "%.0f", targetHours)
                } else {
                    String.format(Locale.US, "%.1f", targetHours)
                }
                val currentHoursDisplay = "${String.format(Locale.US, "%.1f", cutoffHours)} hrs"
                val targetHoursDisplay = "${targetDisplay}+"

                val progressBitmap = GaugeDrawer.drawHorizontalProgressBar(
                    widthPx = 700,
                    heightPx = 68,
                    progress = progress,
                    currentHoursText = currentHoursDisplay,
                    targetHoursText = targetHoursDisplay
                )

                // 5. Populate RemoteViews
                val views = RemoteViews(context.packageName, R.layout.widget_overtime_cutoff).apply {
                    setTextViewText(R.id.widget_badge, "CURRENT CUTOFF")
                    setTextViewText(R.id.widget_cutoff_subtitle, subtitleText)
                    setTextViewText(R.id.widget_total_pay, payString)
                    setImageViewBitmap(R.id.widget_progress_bar_img, progressBitmap)
                    setTextViewText(R.id.widget_hours_rendered, hoursRenderedText)
                    setTextViewText(R.id.widget_action_btn, "+ LOG OT")

                    // Click Intent to open MainActivity
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    setOnClickPendingIntent(R.id.widget_action_btn, pendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widget $appWidgetId", e)
            }
        }
    }
}
