package com.example.ottracker.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class OvertimeRecord(
    val id: String = UUID.randomUUID().toString(),
    val dateMillis: Long, // timestamp for the day
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val hourlyRate: Double = 93.75, // Default warehouse OT rate
    val isHoliday: Boolean = false, // Holiday OT category (+30%)
    val notes: String = ""
) {
    val effectiveHourlyRate: Double
        get() = if (isHoliday) hourlyRate * 1.30 else hourlyRate

    val durationMinutes: Long
        get() = maxOf(0L, (endTimeMillis - startTimeMillis) / (1000 * 60))

    // Rounded DOWN to the nearest 0.5 increment (e.g. 35m → 0.5, 3h35m → 3.5)
    val durationHours: Double
        get() = kotlin.math.floor(durationMinutes / 30.0).toInt() * 0.5

    val totalPay: Double
        get() = durationHours * effectiveHourlyRate

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
            put("isHoliday", isHoliday)
            put("notes", notes)
        }
    }

    companion object {
        // Cached formatters — avoids repeated allocation on every property access
        private val DATE_FORMAT = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        private val SHORT_DATE_FORMAT = SimpleDateFormat("MMM dd", Locale.getDefault())
        private val TIME_FORMAT = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun fromJsonObject(json: JSONObject): OvertimeRecord {
            return OvertimeRecord(
                id = json.optString("id", UUID.randomUUID().toString()),
                dateMillis = json.optLong("dateMillis", System.currentTimeMillis()),
                startTimeMillis = json.optLong("startTimeMillis", System.currentTimeMillis()),
                endTimeMillis = json.optLong("endTimeMillis", System.currentTimeMillis()),
                hourlyRate = json.optDouble("hourlyRate", 93.75),
                isHoliday = json.optBoolean("isHoliday", false),
                notes = json.optString("notes", "")
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
    val targetOtHours: Double = 30.0
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
                targetOtHours = json.optDouble("targetOtHours", 30.0)
            )
        }
    }
}
