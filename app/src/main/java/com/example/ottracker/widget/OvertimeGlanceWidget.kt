package com.example.ottracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.ottracker.MainActivity
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.CutoffHelper
import com.example.ottracker.data.OvertimeRecord
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class OvertimeGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read data from SharedPreferences (lightweight, no Room dependency)
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
        val currentPeriod = CutoffHelper.getCurrentCutoff(
            referenceMillis = System.currentTimeMillis(),
            settings = settings
        )
        val daysLeft = currentPeriod.daysRemaining
        val daysText = if (daysLeft == 1) "1 DAY LEFT" else "$daysLeft DAYS LEFT"
        val cutoffTitle = "CURRENT CUTOFF"
        val cutoffSubtitle = "${currentPeriod.title.uppercase(Locale.US)} · $daysText"

        var cutoffHours = 0.0
        var cutoffPay = 0.0
        for (rec in records) {
            if (rec.startTimeMillis in currentPeriod.startMillis..currentPeriod.endMillis) {
                cutoffHours += rec.durationHours
                cutoffPay += rec.totalPay
            }
        }

        val payString = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", cutoffPay)}"
        val hoursString = "${String.format(Locale.US, "%.1f", cutoffHours)} Hours Rendered"

        // 4. Goal Progress Calculation
        val targetHours = if (settings.targetOtHours > 0.0) settings.targetOtHours else 30.0
        val progress = (cutoffHours / targetHours).toFloat().coerceIn(0.0f, 1.0f)
        val percent = (progress * 100).toInt()
        val targetDisplay = if (targetHours % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", targetHours)
        } else {
            String.format(Locale.US, "%.1f", targetHours)
        }
        val goalLabel = "${String.format(Locale.US, "%.1f", cutoffHours)} / ${targetDisplay} hrs"
        val goalPercentText = "$percent%"

        provideContent {
            OvertimeWidgetContent(
                cutoffTag = cutoffTitle,
                cutoffSubtitle = cutoffSubtitle,
                payString = payString,
                hoursString = hoursString,
                goalLabel = goalLabel,
                goalPercentText = goalPercentText,
                progress = progress
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "ot_tracker_prefs"
        private const val KEY_RECORDS = "records_json"
        private const val KEY_SETTINGS = "settings_json"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE WIDGET UI — Option 1: Dark Gold Cutoff Terminal
// ─────────────────────────────────────────────────────────────────────────────

private val DeepCharcoal = ColorProvider(Color(0xFF141414), Color(0xFF141414))
private val BadgeBg = ColorProvider(Color(0xFF252525), Color(0xFF252525))
private val GoldAccent = ColorProvider(Color(0xFFFFD700), Color(0xFFFFD700))
private val SubtitleGray = ColorProvider(Color(0xFFAAAAAA), Color(0xFFAAAAAA))
private val NearWhite = ColorProvider(Color(0xFFF5F5F5), Color(0xFFF5F5F5))
private val ButtonTextDark = ColorProvider(Color(0xFF0A0A0A), Color(0xFF0A0A0A))
private val GoldButton = ColorProvider(Color(0xFFFFD700), Color(0xFFFFD700))
private val ProgressTrack = ColorProvider(Color(0xFF282828), Color(0xFF282828))

@Composable
private fun OvertimeWidgetContent(
    cutoffTag: String,
    cutoffSubtitle: String,
    payString: String,
    hoursString: String,
    goalLabel: String,
    goalPercentText: String,
    progress: Float
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(18.dp)
            .background(DeepCharcoal)
            .clickable(actionStartActivity<MainActivity>())
            .padding(14.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // ── Row 1: Badge Tag + Subtitle ──
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(6.dp)
                        .background(BadgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cutoffTag,
                        style = TextStyle(
                            color = GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = cutoffSubtitle,
                    style = TextStyle(
                        color = SubtitleGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // ── Row 2: Hero Pay & Hours (Left) + Quick Action (Right) ──
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Currency & Hours
                Column(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Text(
                        text = payString,
                        style = TextStyle(
                            color = GoldAccent,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(1.dp))
                    Text(
                        text = hoursString,
                        style = TextStyle(
                            color = NearWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Right Column: Tactile "+ LOG OT" Button
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(10.dp)
                        .background(GoldButton)
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ LOG OT",
                        style = TextStyle(
                            color = ButtonTextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // ── Row 3: Goal Header Row ──
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CUTOFF GOAL",
                    style = TextStyle(
                        color = SubtitleGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "$goalLabel ($goalPercentText)",
                    style = TextStyle(
                        color = GoldAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // ── Row 4: Horizontal Progress Bar ──
            LinearProgressIndicator(
                progress = progress,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .cornerRadius(3.dp),
                color = GoldAccent,
                backgroundColor = ProgressTrack
            )
        }
    }
}
