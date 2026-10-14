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
        val stats = calculateWarehouseStats(records, settings)
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

    private fun calculateWarehouseStats(records: List<OvertimeRecord>, settings: AppSettings): DashboardStats {
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

        // ── Flexible Cutoff calculation via CutoffHelper ──────────────────────
        val currentCutoffPeriod = com.example.ottracker.data.CutoffHelper.getCurrentCutoff(now.timeInMillis, settings)
        val prevCutoffPeriod = com.example.ottracker.data.CutoffHelper.getPreviousCutoff(now.timeInMillis, settings)
        val recentCutoffPeriods = com.example.ottracker.data.CutoffHelper.getRecentCutoffPeriods(6, settings, now.timeInMillis)

        // Pre-compute 6-month period keys
        data class MonthKey(val year: Int, val month: Int)
        val monthPeriods = mutableListOf<MonthKey>()
        for (offset in 5 downTo 0) {
            val mCal = Calendar.getInstance().apply { add(Calendar.MONTH, -offset) }
            monthPeriods.add(MonthKey(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH)))
        }

        // Initialize trend accumulators — 6 cutoff periods and 6 calendar months
        val cutoffAccum = Array(recentCutoffPeriods.size) { doubleArrayOf(0.0, 0.0) }
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
            val hours = rec.durationHours
            val pay = rec.totalPay

            totalH += hours
            totalP += pay

            // Current & Previous Cutoff matching via precise time window
            if (rec.startTimeMillis in currentCutoffPeriod.startMillis..currentCutoffPeriod.endMillis) {
                cutoffHours += hours
                cutoffPay += pay
            }
            if (rec.startTimeMillis in prevCutoffPeriod.startMillis..prevCutoffPeriod.endMillis) {
                prevCutoffHours += hours
                prevCutoffPay += pay
            }

            // Week check
            if (rec.startTimeMillis in startOfWeekMillis until endOfWeekMillis) {
                weekH += hours; weekP += pay
            }

            // Month check (calendar month)
            if (recYear == currentYear && recMonth == currentMonth) {
                monthH += hours; monthP += pay
            }

            // Today check
            if (recYear == currentYear && recCal.get(Calendar.DAY_OF_YEAR) == currentDayOfYear) {
                todayH += hours; todayP += pay
            }

            // Bucket into cutoff trend periods
            for (i in recentCutoffPeriods.indices) {
                val period = recentCutoffPeriods[i]
                if (rec.startTimeMillis in period.startMillis..period.endMillis) {
                    cutoffAccum[i][0] += hours
                    cutoffAccum[i][1] += pay
                    break
                }
            }

            // Bucket into monthly trend
            monthAccum["${recYear}_${recMonth}"]?.let { arr ->
                arr[0] += hours; arr[1] += pay
            }
        }

        // ── Build cutoff trend list from recentCutoffPeriods ───────────────────
        val cutoffTrends = recentCutoffPeriods.mapIndexed { index, period ->
            val arr = cutoffAccum[index]
            ChartBarData(
                id = "c_$index",
                label = period.title.substringBefore(" –").trim(),
                subLabel = period.title.substringAfter("–").trim(),
                fullTitle = period.title,
                hours = arr[0],
                pay = arr[1],
                isCurrent = (index == recentCutoffPeriods.lastIndex)
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
                title = currentCutoffPeriod.title,
                payday = currentCutoffPeriod.paydayText,
                hours = cutoffHours,
                pay = cutoffPay,
                daysRemaining = currentCutoffPeriod.daysRemaining
            ),
            previousCutoff = CutoffInfo(
                title = prevCutoffPeriod.title,
                payday = prevCutoffPeriod.paydayText,
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
