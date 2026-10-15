package com.example.ottracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun testEveningShiftCrossingTenPm() {
        // 5:00 PM to 11:00 PM (6.0 hours total: 5h Day, 1h Night)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            overtimeType = OvertimeType.REGULAR,
            nightDiffRate = 0.20
        )
        assertTrue(record.hasNightDifferential)
        assertEquals(5.0, record.dayOtHours, 0.001)
        assertEquals(1.0, record.nightOtHours, 0.001)
        assertEquals(468.75, record.dayPay, 0.001)
        assertEquals(112.50, record.nightPay, 0.001)
        assertEquals(581.25, record.totalPay, 0.001)
    }

    @Test
    fun testPureDayShift() {
        // 5:00 PM to 9:00 PM (4.0 hrs Day, 0h Night)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 21, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75
        )
        assertFalse(record.hasNightDifferential)
        assertEquals(4.0, record.dayOtHours, 0.001)
        assertEquals(0.0, record.nightOtHours, 0.001)
        assertEquals(375.0, record.totalPay, 0.001)
    }

    @Test
    fun testHolidayShiftWithNightDifferential() {
        // 5:00 PM to 11:00 PM on Holiday (+30%)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 15, 23, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            overtimeType = OvertimeType.SPECIAL_HOLIDAY,
            nightDiffRate = 0.20
        )
        assertEquals(121.875, record.effectiveDayRate, 0.001)
        assertEquals(146.25, record.effectiveNightRate, 0.001)
        assertEquals(609.375, record.dayPay, 0.001)
        assertEquals(146.25, record.nightPay, 0.001)
        assertEquals(755.625, record.totalPay, 0.001)
    }

    @Test
    fun testShiftCrossingMidnight() {
        // 8:00 PM to 2:00 AM next day (6.0 hours: 2h Day, 4h Night)
        val start = createTimestamp(2026, Calendar.OCTOBER, 15, 20, 0)
        val end = createTimestamp(2026, Calendar.OCTOBER, 16, 2, 0)
        val record = OvertimeRecord(
            dateMillis = start,
            startTimeMillis = start,
            endTimeMillis = end,
            hourlyRate = 93.75,
            overtimeType = OvertimeType.REGULAR,
            nightDiffRate = 0.20
        )
        assertTrue(record.hasNightDifferential)
        assertEquals(2.0, record.dayOtHours, 0.001)
        assertEquals(4.0, record.nightOtHours, 0.001)
        assertEquals(187.50, record.dayPay, 0.001)
        assertEquals(450.00, record.nightPay, 0.001)
        assertEquals(637.50, record.totalPay, 0.001)
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
        assertEquals(0.20, deserialized.nightDiffRate, 0.001)
        assertEquals(581.25, deserialized.totalPay, 0.001)
    }
}
