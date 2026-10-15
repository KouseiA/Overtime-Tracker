package com.example.ottracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CutoffHelperTest {

    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, hour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun testStandardSemiMonthlyCutoff() {
        val settings = AppSettings(
            cutoffFrequency = "SEMI_MONTHLY",
            cutoffFirstEndDay = 15,
            cutoffSecondEndDay = 0
        )

        // Day in first half: Oct 10, 2026
        val oct10 = createTimestamp(2026, Calendar.OCTOBER, 10)
        val cutoff1 = CutoffHelper.getCurrentCutoff(oct10, settings)
        assertEquals("Oct 1 – 15", cutoff1.title)

        // Day in second half: Oct 20, 2026
        val oct20 = createTimestamp(2026, Calendar.OCTOBER, 20)
        val cutoff2 = CutoffHelper.getCurrentCutoff(oct20, settings)
        assertEquals("Oct 16 – 31", cutoff2.title)
    }

    @Test
    fun testBpoCrossMonthSemiMonthlyCutoff() {
        val settings = AppSettings(
            cutoffFrequency = "SEMI_MONTHLY",
            cutoffFirstEndDay = 25,
            cutoffSecondEndDay = 10
        )

        // Day in cycle A: Oct 15, 2026 -> Oct 11 to Oct 25
        val oct15 = createTimestamp(2026, Calendar.OCTOBER, 15)
        val cutoffA = CutoffHelper.getCurrentCutoff(oct15, settings)
        assertEquals("Oct 11 – 25", cutoffA.title)

        // Day in cycle B: Oct 28, 2026 -> Oct 26 to Nov 10
        val oct28 = createTimestamp(2026, Calendar.OCTOBER, 28)
        val cutoffB = CutoffHelper.getCurrentCutoff(oct28, settings)
        assertEquals("Oct 26 – Nov 10", cutoffB.title)

        // Day in cycle B (early next month): Nov 5, 2026 -> Oct 26 to Nov 10
        val nov5 = createTimestamp(2026, Calendar.NOVEMBER, 5)
        val cutoffBEarly = CutoffHelper.getCurrentCutoff(nov5, settings)
        assertEquals("Oct 26 – Nov 10", cutoffBEarly.title)
    }

    @Test
    fun testMonthlyCutoff() {
        // End of Month (0)
        val eomSettings = AppSettings(
            cutoffFrequency = "MONTHLY",
            cutoffMonthlyEndDay = 0
        )
        val oct12 = createTimestamp(2026, Calendar.OCTOBER, 12)
        val eomCutoff = CutoffHelper.getCurrentCutoff(oct12, eomSettings)
        assertEquals("Oct 1 – 31", eomCutoff.title)

        // Custom day: 20th of the month
        val day20Settings = AppSettings(
            cutoffFrequency = "MONTHLY",
            cutoffMonthlyEndDay = 20
        )
        val oct18 = createTimestamp(2026, Calendar.OCTOBER, 18)
        val cutoff20 = CutoffHelper.getCurrentCutoff(oct18, day20Settings)
        assertEquals("Sep 21 – Oct 20", cutoff20.title)
    }

    @Test
    fun testWeeklyCutoff() {
        val weeklySettings = AppSettings(
            cutoffFrequency = "WEEKLY",
            cutoffWeeklyStartDay = Calendar.MONDAY
        )
        // Oct 15, 2026 is a Thursday
        val oct15 = createTimestamp(2026, Calendar.OCTOBER, 15)
        val weeklyCutoff = CutoffHelper.getCurrentCutoff(oct15, weeklySettings)
        // Week of Monday Oct 12 to Sunday Oct 18
        assertEquals("Oct 12 – 18", weeklyCutoff.title)
    }
}
