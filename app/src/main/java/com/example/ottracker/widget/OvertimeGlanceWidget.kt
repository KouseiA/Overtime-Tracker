package com.example.ottracker.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
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
import com.example.ottracker.R
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
        val hoursRenderedText = "${String.format(Locale.US, "%.1f", cutoffHours)} hrs rendered"

        // 4. Goal Progress & Dynamic Tip-Aligned Progress Bitmap
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

        provideContent {
            OvertimeWidgetContent(
                cutoffTag = cutoffTitle,
                cutoffSubtitle = cutoffSubtitle,
                payString = payString,
                hoursRenderedText = hoursRenderedText,
                progressBitmap = progressBitmap
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
// COMPOSABLE WIDGET UI — 100% Concept 1: Dynamic Tip-Aligned Progress Bar
// ─────────────────────────────────────────────────────────────────────────────

private val GoldAccent = ColorProvider(Color(0xFFFFD700), Color(0xFFFFD700))
private val SubtitleGray = ColorProvider(Color(0xFFCBD5E1), Color(0xFFCBD5E1))
private val ButtonTextDark = ColorProvider(Color(0xFF0F141C), Color(0xFF0F141C))
private val GoldButton = ColorProvider(Color(0xFFFFD700), Color(0xFFFFD700))

@Composable
private fun OvertimeWidgetContent(
    cutoffTag: String,
    cutoffSubtitle: String,
    payString: String,
    hoursRenderedText: String,
    progressBitmap: Bitmap
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_terminal_gold_border))
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // ── Row 1: Gold "CURRENT CUTOFF" Pill Badge + Date/Days Subtitle ──
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(8.dp)
                        .background(GoldAccent)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cutoffTag,
                        style = TextStyle(
                            color = ButtonTextDark,
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
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // ── Row 2: Enormous Hero Payout Readout (Matching Mockup) ──
            Text(
                text = payString,
                style = TextStyle(
                    color = GoldAccent,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            // ── Row 3: Dynamic Tip-Aligned Progress Bar & Milestones ──
            Image(
                provider = ImageProvider(progressBitmap),
                contentDescription = "Cutoff Progress",
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            // ── Row 4: Bottom Row: "24.5 hrs rendered" (Left) + "+ LOG OT" Pill (Right) ──
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hoursRenderedText,
                    style = TextStyle(
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(16.dp)
                        .background(GoldButton)
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(horizontal = 18.dp, vertical = 9.dp),
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
        }
    }
}
