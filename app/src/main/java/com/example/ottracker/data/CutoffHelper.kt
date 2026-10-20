package com.example.ottracker.data

import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Immutable
data class CutoffPeriod(
    val startMillis: Long,
    val endMillis: Long, // inclusive end of the cutoff day (23:59:59.999)
    val title: String,
    val paydayText: String,
    val daysRemaining: Int
)

object CutoffHelper {

    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    fun getCurrentCutoff(
        referenceMillis: Long = System.currentTimeMillis(),
        settings: AppSettings
    ): CutoffPeriod {
        return when (settings.cutoffFrequency.uppercase(Locale.US)) {
            "MONTHLY" -> getMonthlyCutoff(referenceMillis, settings.cutoffMonthlyEndDay, offset = 0)
            "WEEKLY" -> getWeeklyCutoff(referenceMillis, settings.cutoffWeeklyStartDay, offset = 0)
            else -> getSemiMonthlyCutoff(
                referenceMillis,
                settings.cutoffFirstEndDay,
                settings.cutoffSecondEndDay,
                offset = 0
            )
        }
    }

    fun getPreviousCutoff(
        referenceMillis: Long = System.currentTimeMillis(),
        settings: AppSettings
    ): CutoffPeriod {
        return when (settings.cutoffFrequency.uppercase(Locale.US)) {
            "MONTHLY" -> getMonthlyCutoff(referenceMillis, settings.cutoffMonthlyEndDay, offset = -1)
            "WEEKLY" -> getWeeklyCutoff(referenceMillis, settings.cutoffWeeklyStartDay, offset = -1)
            else -> getSemiMonthlyCutoff(
                referenceMillis,
                settings.cutoffFirstEndDay,
                settings.cutoffSecondEndDay,
                offset = -1
            )
        }
    }

    fun getRecentCutoffPeriods(
        count: Int,
        settings: AppSettings,
        referenceMillis: Long = System.currentTimeMillis()
    ): List<CutoffPeriod> {
        val list = mutableListOf<CutoffPeriod>()
        for (i in 0 until count) {
            val period = when (settings.cutoffFrequency.uppercase(Locale.US)) {
                "MONTHLY" -> getMonthlyCutoff(referenceMillis, settings.cutoffMonthlyEndDay, offset = -i)
                "WEEKLY" -> getWeeklyCutoff(referenceMillis, settings.cutoffWeeklyStartDay, offset = -i)
                else -> getSemiMonthlyCutoff(
                    referenceMillis,
                    settings.cutoffFirstEndDay,
                    settings.cutoffSecondEndDay,
                    offset = -i
                )
            }
            list.add(period)
        }
        return list.reversed()
    }

    fun isRecordInPeriod(startTimeMillis: Long, period: CutoffPeriod): Boolean {
        return startTimeMillis in period.startMillis..period.endMillis
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEMI-MONTHLY IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────────────────
    private fun getSemiMonthlyCutoff(
        referenceMillis: Long,
        firstEndDayRaw: Int,
        secondEndDayRaw: Int,
        offset: Int // 0 = current, -1 = prev, etc.
    ): CutoffPeriod {
        val firstEnd = if (firstEndDayRaw in 1..28) firstEndDayRaw else 15
        val secondEnd = if (secondEndDayRaw in 1..31) secondEndDayRaw else 0 // 0 = last day

        val refCal = Calendar.getInstance().apply { timeInMillis = referenceMillis }
        val currentDay = refCal.get(Calendar.DAY_OF_MONTH)
        val maxDays = refCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val effectiveSecondEnd = if (secondEnd == 0 || secondEnd > maxDays) maxDays else secondEnd

        // Determine if we are in period 1 or period 2
        // Period 1 is the cycle ending at firstEnd (or between secondEnd+1 and firstEnd if cross-month)
        // Standard: e.g. 15 and 0 (or 30) -> Period 1: 1..15, Period 2: 16..max
        // Cross-month: e.g. firstEnd=25, secondEnd=10 ->
        //   Cycle A: 11..25
        //   Cycle B: 26..10 (spanning into next month)
        val isStandard = firstEnd < effectiveSecondEnd

        val startCal = Calendar.getInstance().apply { timeInMillis = referenceMillis }
        val endCal = Calendar.getInstance().apply { timeInMillis = referenceMillis }

        if (isStandard) {
            val isFirstHalf = currentDay <= firstEnd
            var targetYear = refCal.get(Calendar.YEAR)
            var targetMonth = refCal.get(Calendar.MONTH)
            var targetIsFirst = isFirstHalf

            // Apply step offset (each step is 1 semi-monthly period)
            var remainingSteps = offset
            while (remainingSteps < 0) {
                if (targetIsFirst) {
                    if (targetMonth == 0) { targetMonth = 11; targetYear -= 1 } else { targetMonth -= 1 }
                    targetIsFirst = false
                } else {
                    targetIsFirst = true
                }
                remainingSteps++
            }

            val targetCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, targetYear)
                set(Calendar.MONTH, targetMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val targetMaxDays = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val targetSecondEnd = if (secondEnd == 0 || secondEnd > targetMaxDays) targetMaxDays else secondEnd

            if (targetIsFirst) {
                startCal.set(targetYear, targetMonth, 1, 0, 0, 0)
                startCal.set(Calendar.MILLISECOND, 0)
                endCal.set(targetYear, targetMonth, firstEnd, 23, 59, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            } else {
                startCal.set(targetYear, targetMonth, firstEnd + 1, 0, 0, 0)
                startCal.set(Calendar.MILLISECOND, 0)
                endCal.set(targetYear, targetMonth, targetSecondEnd, 23, 59, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
        } else {
            // Cross-month: e.g. firstEnd = 25, secondEnd = 10
            // Period A: (secondEnd + 1) .. firstEnd (e.g. 11..25)
            // Period B: (firstEnd + 1) .. (secondEnd of next month) (e.g. 26..10)
            val inPeriodA = currentDay in (secondEnd + 1)..firstEnd

            var baseYear = refCal.get(Calendar.YEAR)
            var baseMonth = refCal.get(Calendar.MONTH)
            var currentIsPeriodA = inPeriodA

            // If today is <= secondEnd (e.g. day 5), it belongs to Period B that started previous month
            if (!inPeriodA && currentDay <= secondEnd) {
                // Period B started in previous month
                if (baseMonth == 0) { baseMonth = 11; baseYear -= 1 } else { baseMonth -= 1 }
            }

            var targetYear = baseYear
            var targetMonth = baseMonth
            var targetIsPeriodA = currentIsPeriodA

            var remainingSteps = offset
            while (remainingSteps < 0) {
                if (targetIsPeriodA) {
                    if (targetMonth == 0) { targetMonth = 11; targetYear -= 1 } else { targetMonth -= 1 }
                    targetIsPeriodA = false
                } else {
                    targetIsPeriodA = true
                }
                remainingSteps++
            }

            if (targetIsPeriodA) {
                startCal.set(targetYear, targetMonth, secondEnd + 1, 0, 0, 0)
                startCal.set(Calendar.MILLISECOND, 0)
                endCal.set(targetYear, targetMonth, firstEnd, 23, 59, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            } else {
                startCal.set(targetYear, targetMonth, firstEnd + 1, 0, 0, 0)
                startCal.set(Calendar.MILLISECOND, 0)
                val nextMonthCal = Calendar.getInstance().apply {
                    set(targetYear, targetMonth, 1)
                    add(Calendar.MONTH, 1)
                }
                endCal.set(
                    nextMonthCal.get(Calendar.YEAR),
                    nextMonthCal.get(Calendar.MONTH),
                    secondEnd,
                    23, 59, 59
                )
                endCal.set(Calendar.MILLISECOND, 999)
            }
        }

        val title = formatRangeTitle(startCal, endCal)
        val paydayText = "Payday: " + formatPayday(endCal, daysAfter = 5)
        val daysRemaining = calculateDaysRemaining(endCal, referenceMillis)

        return CutoffPeriod(
            startMillis = startCal.timeInMillis,
            endMillis = endCal.timeInMillis,
            title = title,
            paydayText = paydayText,
            daysRemaining = daysRemaining
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MONTHLY IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────────────────
    private fun getMonthlyCutoff(
        referenceMillis: Long,
        cutoffEndDayRaw: Int,
        offset: Int
    ): CutoffPeriod {
        val refCal = Calendar.getInstance().apply { timeInMillis = referenceMillis }
        val currentDay = refCal.get(Calendar.DAY_OF_MONTH)
        val endDay = if (cutoffEndDayRaw in 1..31) cutoffEndDayRaw else 0 // 0 = end of month

        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()

        if (endDay == 0) {
            // Standard calendar month
            val targetCal = Calendar.getInstance().apply {
                timeInMillis = referenceMillis
                add(Calendar.MONTH, offset)
            }
            val y = targetCal.get(Calendar.YEAR)
            val m = targetCal.get(Calendar.MONTH)
            val maxD = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            startCal.set(y, m, 1, 0, 0, 0)
            startCal.set(Calendar.MILLISECOND, 0)
            endCal.set(y, m, maxD, 23, 59, 59)
            endCal.set(Calendar.MILLISECOND, 999)
        } else {
            // Cutoff on specific day of month (e.g. 25th -> 26th of prev to 25th of curr)
            val baseCal = Calendar.getInstance().apply { timeInMillis = referenceMillis }
            if (currentDay > endDay) {
                // Currently in cycle ending next month on endDay
                baseCal.add(Calendar.MONTH, 1)
            }
            baseCal.add(Calendar.MONTH, offset)

            val endY = baseCal.get(Calendar.YEAR)
            val endM = baseCal.get(Calendar.MONTH)
            val maxD = baseCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualEndDay = if (endDay > maxD) maxD else endDay

            val prevCal = Calendar.getInstance().apply {
                set(endY, endM, 1)
                add(Calendar.MONTH, -1)
            }
            val prevY = prevCal.get(Calendar.YEAR)
            val prevM = prevCal.get(Calendar.MONTH)

            startCal.set(prevY, prevM, actualEndDay + 1, 0, 0, 0)
            startCal.set(Calendar.MILLISECOND, 0)
            endCal.set(endY, endM, actualEndDay, 23, 59, 59)
            endCal.set(Calendar.MILLISECOND, 999)
        }

        val title = formatRangeTitle(startCal, endCal)
        val paydayText = "Payday: " + formatPayday(endCal, daysAfter = 5)
        val daysRemaining = calculateDaysRemaining(endCal, referenceMillis)

        return CutoffPeriod(
            startMillis = startCal.timeInMillis,
            endMillis = endCal.timeInMillis,
            title = title,
            paydayText = paydayText,
            daysRemaining = daysRemaining
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEEKLY IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────────────────
    private fun getWeeklyCutoff(
        referenceMillis: Long,
        startDayOfWeek: Int, // Calendar.MONDAY, etc.
        offset: Int
    ): CutoffPeriod {
        val startCal = Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            // Go to beginning of week based on startDayOfWeek
            while (get(Calendar.DAY_OF_WEEK) != startDayOfWeek) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.WEEK_OF_YEAR, offset)
        }

        val endCal = Calendar.getInstance().apply {
            timeInMillis = startCal.timeInMillis
            add(Calendar.DAY_OF_MONTH, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val title = formatRangeTitle(startCal, endCal)
        val paydayText = "Payday: " + formatPayday(endCal, daysAfter = 2)
        val daysRemaining = calculateDaysRemaining(endCal, referenceMillis)

        return CutoffPeriod(
            startMillis = startCal.timeInMillis,
            endMillis = endCal.timeInMillis,
            title = title,
            paydayText = paydayText,
            daysRemaining = daysRemaining
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FORMATTING HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private fun formatRangeTitle(startCal: Calendar, endCal: Calendar): String {
        val startMonth = monthFormat.format(startCal.time)
        val endMonth = monthFormat.format(endCal.time)
        val startDay = startCal.get(Calendar.DAY_OF_MONTH)
        val endDay = endCal.get(Calendar.DAY_OF_MONTH)

        return if (startMonth == endMonth) {
            "$startMonth $startDay – $endDay"
        } else {
            "$startMonth $startDay – $endMonth $endDay"
        }
    }

    private fun formatPayday(endCal: Calendar, daysAfter: Int): String {
        val payCal = Calendar.getInstance().apply {
            timeInMillis = endCal.timeInMillis
            add(Calendar.DAY_OF_MONTH, daysAfter)
        }
        return fullDateFormat.format(payCal.time)
    }

    private fun calculateDaysRemaining(endCal: Calendar, referenceMillis: Long): Int {
        val diffMillis = endCal.timeInMillis - referenceMillis
        val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        return if (days < 0) 0 else days
    }
}
