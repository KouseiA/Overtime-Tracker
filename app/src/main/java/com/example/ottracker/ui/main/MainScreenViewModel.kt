package com.example.ottracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.OvertimeRecord
import com.example.ottracker.data.OvertimeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CutoffInfo(
    val title: String = "",
    val payday: String = "",
    val hours: Double = 0.0,
    val pay: Double = 0.0,
    val daysRemaining: Int = 0
)

data class ChartBarData(
    val id: String = "",
    val label: String = "",
    val subLabel: String = "",
    val fullTitle: String = "",
    val hours: Double = 0.0,
    val pay: Double = 0.0,
    val isCurrent: Boolean = false
)

data class DashboardStats(
    val currentCutoff: CutoffInfo = CutoffInfo(),
    val previousCutoff: CutoffInfo = CutoffInfo(),
    val todayHours: Double = 0.0,
    val todayPay: Double = 0.0,
    val weekHours: Double = 0.0,
    val weekPay: Double = 0.0,
    val monthHours: Double = 0.0,
    val monthPay: Double = 0.0,
    val totalHours: Double = 0.0,
    val totalPay: Double = 0.0,
    val cutoffTrends: List<ChartBarData> = emptyList(),
    val monthlyTrends: List<ChartBarData> = emptyList()
)

data class MainUiState(
    val records: List<OvertimeRecord> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val stats: DashboardStats = DashboardStats()
)

class MainScreenViewModel(private val repository: OvertimeRepository) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        repository.records,
        repository.settings
    ) { records, settings ->
        val stats = calculateWarehouseStats(records)
        MainUiState(
            records = records,
            settings = settings,
            stats = stats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun addOrUpdateRecord(record: OvertimeRecord) {
        viewModelScope.launch {
            repository.addRecord(record)
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun importBackupJson(jsonStr: String, replaceExisting: Boolean = false): Result<Int> {
        return runCatching {
            repository.importBackupJson(jsonStr, replaceExisting)
        }
    }

    private fun calculateWarehouseStats(records: List<OvertimeRecord>): DashboardStats {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH)
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        val currentDayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val maxDaysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val isFirstHalf = currentDay <= 15

        // Work week boundaries: Monday 00:00:00 to next Monday 00:00:00
        val startOfWeekCal = (now.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeekMillis = startOfWeekCal.timeInMillis
        val endOfWeekMillis = (startOfWeekCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis

        val monthNameFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val currentMonthName = monthNameFormat.format(now.time)

        // Current cutoff metadata
        val currentCutoffTitle: String
        val currentPaydayText: String
        val daysRemainingInCutoff: Int
        val nextMonthName = monthNameFormat.format(
            Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time
        )

        if (isFirstHalf) {
            currentCutoffTitle = "$currentMonthName 1 – 15"
            currentPaydayText = "Payday: $currentMonthName 20"
            daysRemainingInCutoff = 15 - currentDay
        } else {
            currentCutoffTitle = "$currentMonthName 16 – $maxDaysInMonth"
            currentPaydayText = "Payday: $nextMonthName 5"
            daysRemainingInCutoff = maxDaysInMonth - currentDay
        }

        // Previous cutoff metadata
        val prevCutoffTitle: String
        val prevPaydayText: String
        val prevMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val prevMonthName = monthNameFormat.format(prevMonthCal.time)
        val prevMonthMaxDays = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val prevMonthIndex = prevMonthCal.get(Calendar.MONTH)
        val prevMonthYear = prevMonthCal.get(Calendar.YEAR)

        if (isFirstHalf) {
            prevCutoffTitle = "$prevMonthName 16 – $prevMonthMaxDays"
            prevPaydayText = "Paid on: $currentMonthName 5"
        } else {
            prevCutoffTitle = "$currentMonthName 1 – 15"
            prevPaydayText = "Paid on: $currentMonthName 20"
        }

        // ── Pre-compute 6-cutoff period keys for bucket lookup ───────────────
        val cutoffPeriods = mutableListOf<Triple<Int, Int, Boolean>>()
        var stepYear = currentYear
        var stepMonth = currentMonth
        var stepIsFirstHalf = isFirstHalf
        for (i in 0 until 6) {
            cutoffPeriods.add(Triple(stepYear, stepMonth, stepIsFirstHalf))
            if (stepIsFirstHalf) {
                if (stepMonth == 0) { stepMonth = 11; stepYear -= 1 } else { stepMonth -= 1 }
                stepIsFirstHalf = false
            } else {
                stepIsFirstHalf = true
            }
        }
        cutoffPeriods.reverse()

        // Pre-compute 6-month period keys
        data class MonthKey(val year: Int, val month: Int)
        val monthPeriods = mutableListOf<MonthKey>()
        for (offset in 5 downTo 0) {
            val mCal = Calendar.getInstance().apply { add(Calendar.MONTH, -offset) }
            monthPeriods.add(MonthKey(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH)))
        }

        // Initialize trend accumulators — only for the 6 periods we care about
        val cutoffAccum = HashMap<String, DoubleArray>(12)
        for ((y, m, fh) in cutoffPeriods) cutoffAccum["${y}_${m}_$fh"] = doubleArrayOf(0.0, 0.0)
        val monthAccum = HashMap<String, DoubleArray>(12)
        for (mk in monthPeriods) monthAccum["${mk.year}_${mk.month}"] = doubleArrayOf(0.0, 0.0)

        // ── SINGLE PASS over all records ─────────────────────────────────────
        var cutoffHours = 0.0; var cutoffPay = 0.0
        var prevCutoffHours = 0.0; var prevCutoffPay = 0.0
        var todayH = 0.0; var todayP = 0.0
        var weekH = 0.0; var weekP = 0.0
        var monthH = 0.0; var monthP = 0.0
        var totalH = 0.0; var totalP = 0.0

        // Reuse a single Calendar instance for all record parsing
        val recCal = Calendar.getInstance()

        for (rec in records) {
            recCal.timeInMillis = rec.startTimeMillis
            val recYear = recCal.get(Calendar.YEAR)
            val recMonth = recCal.get(Calendar.MONTH)
            val recDay = recCal.get(Calendar.DAY_OF_MONTH)
            val hours = rec.durationHours
            val pay = rec.totalPay

            totalH += hours
            totalP += pay

            // Week check
            if (rec.startTimeMillis in startOfWeekMillis until endOfWeekMillis) {
                weekH += hours; weekP += pay
            }

            if (recYear == currentYear) {
                if (recMonth == currentMonth) {
                    // Current cutoff
                    if (isFirstHalf && recDay in 1..15) { cutoffHours += hours; cutoffPay += pay }
                    else if (!isFirstHalf && recDay >= 16) { cutoffHours += hours; cutoffPay += pay }
                    monthH += hours; monthP += pay
                }
                if (recCal.get(Calendar.DAY_OF_YEAR) == currentDayOfYear) {
                    todayH += hours; todayP += pay
                }
            }

            // Previous cutoff (single-pass instead of separate loop)
            if (isFirstHalf) {
                if (recYear == prevMonthYear && recMonth == prevMonthIndex && recDay >= 16) {
                    prevCutoffHours += hours; prevCutoffPay += pay
                }
            } else {
                if (recYear == currentYear && recMonth == currentMonth && recDay in 1..15) {
                    prevCutoffHours += hours; prevCutoffPay += pay
                }
            }

            // Bucket into cutoff trend (O(1) HashMap lookup instead of nested loop)
            val recIsFirstHalf = recDay <= 15
            cutoffAccum["${recYear}_${recMonth}_$recIsFirstHalf"]?.let { arr ->
                arr[0] += hours; arr[1] += pay
            }

            // Bucket into monthly trend
            monthAccum["${recYear}_${recMonth}"]?.let { arr ->
                arr[0] += hours; arr[1] += pay
            }
        }

        // ── Build cutoff trend list from pre-accumulated buckets ─────────────
        val cutoffTrends = cutoffPeriods.map { (y, m, firstHalf) ->
            val tempCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, 1)
            }
            val mName = monthNameFormat.format(tempCal.time)
            val maxD = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val arr = cutoffAccum["${y}_${m}_$firstHalf"] ?: doubleArrayOf(0.0, 0.0)

            ChartBarData(
                id = "c_${y}_${m}_$firstHalf",
                label = if (firstHalf) "$mName 1" else "$mName 2",
                subLabel = if (firstHalf) "1–15" else "16–$maxD",
                fullTitle = if (firstHalf) "$mName 1–15, $y" else "$mName 16–$maxD, $y",
                hours = arr[0],
                pay = arr[1],
                isCurrent = (y == currentYear && m == currentMonth && firstHalf == isFirstHalf)
            )
        }

        // ── Build monthly trend list from pre-accumulated buckets ────────────
        val fullMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthlyTrends = monthPeriods.mapIndexed { index, mk ->
            val mCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, mk.year); set(Calendar.MONTH, mk.month); set(Calendar.DAY_OF_MONTH, 1)
            }
            val arr = monthAccum["${mk.year}_${mk.month}"] ?: doubleArrayOf(0.0, 0.0)

            ChartBarData(
                id = "m_${mk.year}_${mk.month}",
                label = monthNameFormat.format(mCal.time),
                subLabel = "${mk.year}",
                fullTitle = fullMonthFormat.format(mCal.time),
                hours = arr[0],
                pay = arr[1],
                isCurrent = (index == monthPeriods.lastIndex)
            )
        }

        return DashboardStats(
            currentCutoff = CutoffInfo(
                title = currentCutoffTitle,
                payday = currentPaydayText,
                hours = cutoffHours,
                pay = cutoffPay,
                daysRemaining = daysRemainingInCutoff
            ),
            previousCutoff = CutoffInfo(
                title = prevCutoffTitle,
                payday = prevPaydayText,
                hours = prevCutoffHours,
                pay = prevCutoffPay
            ),
            todayHours = todayH,
            todayPay = todayP,
            weekHours = weekH,
            weekPay = weekP,
            monthHours = monthH,
            monthPay = monthP,
            totalHours = totalH,
            totalPay = totalP,
            cutoffTrends = cutoffTrends,
            monthlyTrends = monthlyTrends
        )
    }

    class Factory(private val repository: OvertimeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainScreenViewModel(repository) as T
        }
    }
}
