package com.example.ottracker.ui.main

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.OvertimeRecord
import com.example.ottracker.data.OvertimeRepository
import com.example.ottracker.data.calculateTotalPay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Immutable
data class CutoffInfo(
    val title: String = "",
    val payday: String = "",
    val hours: Double = 0.0,
    val pay: Double = 0.0,
    val daysRemaining: Int = 0
)

@Immutable
data class ChartBarData(
    val id: String = "",
    val label: String = "",
    val subLabel: String = "",
    val fullTitle: String = "",
    val hours: Double = 0.0,
    val pay: Double = 0.0,
    val isCurrent: Boolean = false
)

@Immutable
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

@Immutable
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
        val cutoffTrendBuckets = Array(recentCutoffPeriods.size) { mutableListOf<OvertimeRecord>() }
        val monthTrendBuckets = HashMap<String, MutableList<OvertimeRecord>>(12)
        for (mk in monthPeriods) monthTrendBuckets["${mk.year}_${mk.month}"] = mutableListOf()

        val cutoffRecords = mutableListOf<OvertimeRecord>()
        val prevCutoffRecords = mutableListOf<OvertimeRecord>()
        val todayRecords = mutableListOf<OvertimeRecord>()
        val weekRecords = mutableListOf<OvertimeRecord>()
        val monthRecords = mutableListOf<OvertimeRecord>()

        var totalH = 0.0

        // Reuse a single Calendar instance for all record parsing
        val recCal = Calendar.getInstance()

        for (rec in records) {
            recCal.timeInMillis = rec.startTimeMillis
            val recYear = recCal.get(Calendar.YEAR)
            val recMonth = recCal.get(Calendar.MONTH)
            val hours = rec.durationHours

            totalH += hours

            // Current & Previous Cutoff matching via precise time window
            if (rec.startTimeMillis in currentCutoffPeriod.startMillis..currentCutoffPeriod.endMillis) {
                cutoffRecords.add(rec)
            }
            if (rec.startTimeMillis in prevCutoffPeriod.startMillis..prevCutoffPeriod.endMillis) {
                prevCutoffRecords.add(rec)
            }

            // Week check
            if (rec.startTimeMillis in startOfWeekMillis until endOfWeekMillis) {
                weekRecords.add(rec)
            }

            // Month check (calendar month)
            if (recYear == currentYear && recMonth == currentMonth) {
                monthRecords.add(rec)
            }

            // Today check
            if (recYear == currentYear && recCal.get(Calendar.DAY_OF_YEAR) == currentDayOfYear) {
                todayRecords.add(rec)
            }

            // Bucket into cutoff trend periods
            for (i in recentCutoffPeriods.indices) {
                val period = recentCutoffPeriods[i]
                if (rec.startTimeMillis in period.startMillis..period.endMillis) {
                    cutoffTrendBuckets[i].add(rec)
                    break
                }
            }

            // Bucket into monthly trend
            monthTrendBuckets["${recYear}_${recMonth}"]?.add(rec)
        }

        val cutoffHours = cutoffRecords.sumOf { it.durationHours }
        val cutoffPay = cutoffRecords.calculateTotalPay()
        val prevCutoffHours = prevCutoffRecords.sumOf { it.durationHours }
        val prevCutoffPay = prevCutoffRecords.calculateTotalPay()
        val todayH = todayRecords.sumOf { it.durationHours }
        val todayP = todayRecords.calculateTotalPay()
        val weekH = weekRecords.sumOf { it.durationHours }
        val weekP = weekRecords.calculateTotalPay()
        val monthH = monthRecords.sumOf { it.durationHours }
        val monthP = monthRecords.calculateTotalPay()
        val totalP = records.calculateTotalPay()

        // ── Build cutoff trend list from recentCutoffPeriods ───────────────────
        val cutoffTrends = recentCutoffPeriods.mapIndexed { index, period ->
            val bucket = cutoffTrendBuckets[index]
            ChartBarData(
                id = "c_$index",
                label = period.title.substringBefore(" –").trim(),
                subLabel = period.title.substringAfter("–").trim(),
                fullTitle = period.title,
                hours = bucket.sumOf { it.durationHours },
                pay = bucket.calculateTotalPay(),
                isCurrent = (index == recentCutoffPeriods.lastIndex)
            )
        }

        // ── Build monthly trend list from pre-accumulated buckets ────────────
        val fullMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthlyTrends = monthPeriods.mapIndexed { index, mk ->
            val mCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, mk.year); set(Calendar.MONTH, mk.month); set(Calendar.DAY_OF_MONTH, 1)
            }
            val bucket = monthTrendBuckets["${mk.year}_${mk.month}"] ?: emptyList()

            ChartBarData(
                id = "m_${mk.year}_${mk.month}",
                label = monthNameFormat.format(mCal.time),
                subLabel = "${mk.year}",
                fullTitle = fullMonthFormat.format(mCal.time),
                hours = bucket.sumOf { it.durationHours },
                pay = bucket.calculateTotalPay(),
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
