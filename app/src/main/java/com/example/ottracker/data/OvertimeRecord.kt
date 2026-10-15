package com.example.ottracker.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * DOLE-compliant OT rate category constants and multipliers.
 *
 * Regular OT:           ×1.00  (base OT rate as-is)
 * Special Non-Working Holiday OT: ×1.30  (+30%)
 * Regular Holiday OT:   ×2.00  (+100%, double pay)
 */
object OvertimeType {
    const val REGULAR = "REGULAR"
    const val SPECIAL_HOLIDAY = "SPECIAL_HOLIDAY"
    const val REGULAR_HOLIDAY = "REGULAR_HOLIDAY"

    fun multiplierFor(type: String): Double = when (type) {
        SPECIAL_HOLIDAY -> 1.30
        REGULAR_HOLIDAY -> 2.00
        else -> 1.00
    }

    fun labelFor(type: String): String = when (type) {
        SPECIAL_HOLIDAY -> "Special Holiday OT"
        REGULAR_HOLIDAY -> "Regular Holiday OT"
        else -> "Regular OT"
    }

    fun shortLabelFor(type: String): String = when (type) {
        SPECIAL_HOLIDAY -> "SNWH"
        REGULAR_HOLIDAY -> "RH"
        else -> "REG"
    }

    fun emojiFor(type: String): String = when (type) {
        SPECIAL_HOLIDAY -> "🏖️"
        REGULAR_HOLIDAY -> "🎉"
        else -> "⚡"
    }
}

data class OvertimeRecord(
    val id: String = UUID.randomUUID().toString(),
    val dateMillis: Long, // timestamp for the day
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val hourlyRate: Double = 93.75, // Default warehouse OT rate
    val overtimeType: String = OvertimeType.REGULAR, // DOLE OT category
    val notes: String = "",
    val nightDiffRate: Double = 0.20 // Night Shift Differential rate (default +20%)
) {
    // Legacy compatibility alias
    val isHoliday: Boolean
        get() = overtimeType != OvertimeType.REGULAR

    val durationMinutes: Long
        get() = maxOf(0L, (endTimeMillis - startTimeMillis) / (1000 * 60))

    // Rounded DOWN to the nearest 0.5 increment (e.g. 35m → 0.5, 3h35m → 3.5)
    val durationHours: Double
        get() = kotlin.math.floor(durationMinutes / 30.0).toInt() * 0.5

    // Splitting 30-min increments between Day OT (06:00 - 22:00) and Night OT (22:00 - 06:00)
    private fun computeOtSplit(): Pair<Double, Double> {
        val totalBlocks = (durationMinutes / 30).toInt()
        if (totalBlocks <= 0) return Pair(0.0, 0.0)

        var dayBlocks = 0
        var nightBlocks = 0
        val cal = java.util.Calendar.getInstance()

        for (i in 0 until totalBlocks) {
            val midpointMillis = startTimeMillis + (i * 30L + 15L) * 60L * 1000L
            cal.timeInMillis = midpointMillis
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            if (hour >= 22 || hour < 6) {
                nightBlocks++
            } else {
                dayBlocks++
            }
        }
        return Pair(dayBlocks * 0.5, nightBlocks * 0.5)
    }

    val dayOtHours: Double
        get() = computeOtSplit().first

    val nightOtHours: Double
        get() = computeOtSplit().second

    val hasNightDifferential: Boolean
        get() = nightOtHours > 0.0

    /** Day rate = base OT rate × category multiplier */
    val effectiveDayRate: Double
        get() = hourlyRate * OvertimeType.multiplierFor(overtimeType)

    /** Backward-compat alias used by widgets and dashboard */
    val effectiveHourlyRate: Double
        get() = effectiveDayRate

    /** Night rate = day rate × (1 + NSD%) */
    val effectiveNightRate: Double
        get() = effectiveDayRate * (1.0 + nightDiffRate)

    val dayPay: Double
        get() = dayOtHours * effectiveDayRate

    val nightPay: Double
        get() = nightOtHours * effectiveNightRate

    val totalPay: Double
        get() = dayPay + nightPay

    /** Category multiplier for display (e.g., "×1.30") */
    val categoryMultiplier: Double
        get() = OvertimeType.multiplierFor(overtimeType)

    /** Formatted category label */
    val categoryLabel: String
        get() = OvertimeType.labelFor(overtimeType)

    val formattedFormula: String
        get() = String.format(Locale.US, "₱%.2f × %.2f = ₱%.2f/hr", effectiveDayRate, 1.0 + nightDiffRate, effectiveNightRate)

    val formattedHoursDecimal: String
        get() = String.format(Locale.US, "%.2f hrs", durationHours)

    val formattedDurationText: String
        get() {
            val h = durationMinutes / 60
            val m = durationMinutes % 60
            return when {
                h > 0 && m > 0 -> "${h}h ${m}m"
                h > 0 -> "${h} hr${if (h > 1) "s" else ""}"
                else -> "${m} mins"
            }
        }

    val formattedDate: String
        get() = DATE_FORMAT.format(Date(dateMillis))

    val formattedShortDate: String
        get() = SHORT_DATE_FORMAT.format(Date(dateMillis))

    val formattedTimeRange: String
        get() = "${TIME_FORMAT.format(Date(startTimeMillis))} - ${TIME_FORMAT.format(Date(endTimeMillis))}"

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("dateMillis", dateMillis)
            put("startTimeMillis", startTimeMillis)
            put("endTimeMillis", endTimeMillis)
            put("hourlyRate", hourlyRate)
            put("overtimeType", overtimeType)
            // Legacy field kept for older app versions reading the backup
            put("isHoliday", isHoliday)
            put("notes", notes)
            put("nightDiffRate", nightDiffRate)
        }
    }

    companion object {
        // Cached formatters — avoids repeated allocation on every property access
        private val DATE_FORMAT = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        private val SHORT_DATE_FORMAT = SimpleDateFormat("MMM dd", Locale.getDefault())
        private val TIME_FORMAT = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun fromJsonObject(json: JSONObject): OvertimeRecord {
            // Migration: if overtimeType exists, use it; else fall back to isHoliday boolean
            val overtimeType = if (json.has("overtimeType")) {
                json.optString("overtimeType", OvertimeType.REGULAR)
            } else {
                // Legacy migration: isHoliday=true → SPECIAL_HOLIDAY
                if (json.optBoolean("isHoliday", false)) OvertimeType.SPECIAL_HOLIDAY
                else OvertimeType.REGULAR
            }

            return OvertimeRecord(
                id = json.optString("id", UUID.randomUUID().toString()),
                dateMillis = json.optLong("dateMillis", System.currentTimeMillis()),
                startTimeMillis = json.optLong("startTimeMillis", System.currentTimeMillis()),
                endTimeMillis = json.optLong("endTimeMillis", System.currentTimeMillis()),
                hourlyRate = json.optDouble("hourlyRate", 93.75),
                overtimeType = overtimeType,
                notes = json.optString("notes", ""),
                nightDiffRate = json.optDouble("nightDiffRate", 0.20)
            )
        }
    }
}

data class AppSettings(
    val baseHourlyRate: Double = 93.75, // Default OT rate ₱93.75 / hr
    val currencySymbol: String = "₱",
    val defaultStartHour: Int = 17, // 5:00 PM
    val defaultStartMinute: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20, // 8:00 PM
    val reminderMinute: Int = 0,
    // Cutoff schedule configuration:
    // frequency: "SEMI_MONTHLY", "MONTHLY", "WEEKLY"
    val cutoffFrequency: String = "SEMI_MONTHLY",
    // Semi-monthly: 1st cutoff ends on this day (e.g., 15)
    val cutoffFirstEndDay: Int = 15,
    // Semi-monthly: 2nd cutoff ends on this day (0 = end of month, or e.g. 10 if 26th-10th)
    val cutoffSecondEndDay: Int = 0,
    // Monthly: cutoff day (0 = end of month, or e.g. 25 for 26th-25th)
    val cutoffMonthlyEndDay: Int = 0,
    // Weekly: start day of week (Calendar.MONDAY = 2, Calendar.SUNDAY = 1, etc.)
    val cutoffWeeklyStartDay: Int = java.util.Calendar.MONDAY,
    // Target OT Hours per Cutoff (for progress tracking & 2x2 widget circular gauge)
    val targetOtHours: Double = 30.0,
    // Night Shift Differential settings (10:00 PM - 6:00 AM)
    val nightDiffEnabled: Boolean = true,
    val nightDiffRate: Double = 0.20
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("baseHourlyRate", baseHourlyRate)
            put("currencySymbol", currencySymbol)
            put("defaultStartHour", defaultStartHour)
            put("defaultStartMinute", defaultStartMinute)
            put("reminderEnabled", reminderEnabled)
            put("reminderHour", reminderHour)
            put("reminderMinute", reminderMinute)
            put("cutoffFrequency", cutoffFrequency)
            put("cutoffFirstEndDay", cutoffFirstEndDay)
            put("cutoffSecondEndDay", cutoffSecondEndDay)
            put("cutoffMonthlyEndDay", cutoffMonthlyEndDay)
            put("cutoffWeeklyStartDay", cutoffWeeklyStartDay)
            put("targetOtHours", targetOtHours)
            put("nightDiffEnabled", nightDiffEnabled)
            put("nightDiffRate", nightDiffRate)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): AppSettings {
            return AppSettings(
                baseHourlyRate = json.optDouble("baseHourlyRate", 93.75),
                currencySymbol = json.optString("currencySymbol", "₱"),
                defaultStartHour = json.optInt("defaultStartHour", 17),
                defaultStartMinute = json.optInt("defaultStartMinute", 0),
                reminderEnabled = json.optBoolean("reminderEnabled", false),
                reminderHour = json.optInt("reminderHour", 20),
                reminderMinute = json.optInt("reminderMinute", 0),
                cutoffFrequency = json.optString("cutoffFrequency", "SEMI_MONTHLY"),
                cutoffFirstEndDay = json.optInt("cutoffFirstEndDay", 15),
                cutoffSecondEndDay = json.optInt("cutoffSecondEndDay", 0),
                cutoffMonthlyEndDay = json.optInt("cutoffMonthlyEndDay", 0),
                cutoffWeeklyStartDay = json.optInt("cutoffWeeklyStartDay", java.util.Calendar.MONDAY),
                targetOtHours = json.optDouble("targetOtHours", 30.0),
                nightDiffEnabled = json.optBoolean("nightDiffEnabled", true),
                nightDiffRate = json.optDouble("nightDiffRate", 0.20)
            )
        }
    }
}
