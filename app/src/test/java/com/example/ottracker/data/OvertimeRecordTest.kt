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
    fun testRecordKeepsHalfHour() {
        // 5:00 PM to 6:30 PM (90 mins) -> 1.5 hours recorded
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
        assertEquals(1.5, record.durationHours, 0.001)
        assertEquals(150.0, record.totalPay, 0.001)
    }

    @Test
    fun testTwoHalfHoursCombineIntoFullHour() {
        // Shift 1: 1.5 hrs @ 100/hr = 150
        // Shift 2: 2.5 hrs @ 100/hr = 250
        // Total = 4.0 hrs -> 400.0 total pay (no leftover .50)
        val start1 = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end1 = createTimestamp(2026, Calendar.OCTOBER, 15, 18, 30)
        val shift1 = OvertimeRecord(dateMillis = start1, startTimeMillis = start1, endTimeMillis = end1, hourlyRate = 100.0)

        val start2 = createTimestamp(2026, Calendar.OCTOBER, 16, 17, 0)
        val end2 = createTimestamp(2026, Calendar.OCTOBER, 16, 19, 30)
        val shift2 = OvertimeRecord(dateMillis = start2, startTimeMillis = start2, endTimeMillis = end2, hourlyRate = 100.0)

        val records = listOf(shift1, shift2)
        assertEquals(4.0, records.sumOf { it.durationHours }, 0.001)
        assertEquals(400.0, records.calculateTotalPay(), 0.001)
    }

    @Test
    fun testLeftoverHalfHourComputedInTotal() {
        // Shift 1: 1.5 hrs @ 100/hr = 150
        // Shift 2: 2.0 hrs @ 100/hr = 200
        // Total = 3.5 hrs -> pays for all 3.5 hrs = 350.0 (leftover .50 is computed)
        val start1 = createTimestamp(2026, Calendar.OCTOBER, 15, 17, 0)
        val end1 = createTimestamp(2026, Calendar.OCTOBER, 15, 18, 30)
        val shift1 = OvertimeRecord(dateMillis = start1, startTimeMillis = start1, endTimeMillis = end1, hourlyRate = 100.0)

        val start2 = createTimestamp(2026, Calendar.OCTOBER, 16, 17, 0)
        val end2 = createTimestamp(2026, Calendar.OCTOBER, 16, 19, 0)
        val shift2 = OvertimeRecord(dateMillis = start2, startTimeMillis = start2, endTimeMillis = end2, hourlyRate = 100.0)

        val records = listOf(shift1, shift2)
        assertEquals(3.5, records.sumOf { it.durationHours }, 0.001)
        assertEquals(350.0, records.calculateTotalPay(), 0.001)
    }

    @Test
    fun testSevenPointFiveHoursPaysForFullSevenPointFive() {
        // Three 2.5h shifts = 7.5 hrs total @ 100/hr
        // Fully computed: 7.5 hrs × 100 = 750.0
        val shifts = (1..3).map { i ->
            val s = createTimestamp(2026, Calendar.OCTOBER, i, 17, 0)
            val e = createTimestamp(2026, Calendar.OCTOBER, i, 19, 30)
            OvertimeRecord(dateMillis = s, startTimeMillis = s, endTimeMillis = e, hourlyRate = 100.0)
        }
        assertEquals(7.5, shifts.sumOf { it.durationHours }, 0.001)
        assertEquals(750.0, shifts.calculateTotalPay(), 0.001)
    }
}
