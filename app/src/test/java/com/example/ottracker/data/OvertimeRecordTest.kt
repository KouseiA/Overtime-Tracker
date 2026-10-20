package com.example.ottracker.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class OvertimeRecordTest {
    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun testRegularOvertimeShift() {
        // 5:00 PM to 11:00 PM (6.0 hours total)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            overtimeType = OvertimeType.REGULAR
        )
        assertEquals(6.0, record.durationHours, 0.001)
        assertEquals(93.75, record.effectiveHourlyRate, 0.001)
        assertEquals(562.50, record.totalPay, 0.001)
    }

    @Test
    fun testPureDayShift() {
        // 5:00 PM to 9:00 PM (4.0 hrs)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 21, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75
        )
        assertEquals(4.0, record.durationHours, 0.001)
        assertEquals(375.0, record.totalPay, 0.001)
    }

    @Test
    fun testSpecialHolidayShift() {
        // 5:00 PM to 11:00 PM on Special Holiday (+30%)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            overtimeType = OvertimeType.SPECIAL_HOLIDAY
        )
        assertEquals(1.30, record.categoryMultiplier, 0.001)
        assertEquals(121.875, record.effectiveHourlyRate, 0.001)
        assertEquals(731.25, record.totalPay, 0.001)
    }

    @Test
    fun testShiftCrossingMidnight() {
        // 8:00 PM to 2:00 AM next day (6.0 hours)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 20, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 16, 2, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            overtimeType = OvertimeType.REGULAR
        )
        assertEquals(6.0, record.durationHours, 0.001)
        // All hours paid at effective rate: 93.75 × 1.00 × 6 = 562.50
        assertEquals(562.50, record.totalPay, 0.001)
    }

    @Test
    fun testJsonBackwardCompatibility() {
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val legacyJson = org.json.JSONObject().apply {
            put("id", "legacy-id")
            put("dateMillis", start)
            put("startTimeMillis", start)
            put("endTimeMillis", end)
            put("hourlyRate", 93.75)
            put("isHoliday", false)
            put("notes", "")
        }
        val deserialized = OvertimeRecord.fromJsonObject(legacyJson)
        // 6.0 hrs × 93.75 × 1.00 = 562.50
        assertEquals(562.50, deserialized.totalPay, 0.001)
    }

    @Test
    fun testHalfHourFractionDropped() {
        // 5:00 PM to 6:30 PM (1h 30m) -> only 1.0 hour computed (no .50)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 18, 30)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 100.0,
            overtimeType = OvertimeType.REGULAR
        )
        assertEquals(90L, record.durationMinutes)
        assertEquals(1.0, record.durationHours, 0.001)
        assertEquals(100.0, record.totalPay, 0.001)
    }

    @Test
    fun testUnderOneHourYieldsZero() {
        // 5:00 PM to 5:45 PM (45m) -> 0.0 hours computed
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 45)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 100.0,
            overtimeType = OvertimeType.REGULAR
        )
        assertEquals(45L, record.durationMinutes)
        assertEquals(0.0, record.durationHours, 0.001)
        assertEquals(0.0, record.totalPay, 0.001)
    }
}
