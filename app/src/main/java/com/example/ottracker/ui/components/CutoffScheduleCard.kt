package com.example.ottracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.CutoffHelper
import com.example.ottracker.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CutoffScheduleCard(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Dialog state for picking custom days
    var showDayPickerDialog by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf<String?>(null) } // "SEMI_1", "SEMI_2", "MONTHLY"
    var pickerTitle by remember { mutableStateOf("") }
    var pickerAllowEom by remember { mutableStateOf(false) }
    var pickerMaxDays by remember { mutableStateOf(31) }
    var pickerSelectedDay by remember { mutableStateOf(0) }

    NeoCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card Header ──────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Payroll Cutoff Schedule",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    Text(
                        text = "Customize your pay cycle and cutoff dates",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }
            }

            // ── Pay Frequency Selector Chips ─────────────────────────────────
            Text(
                text = "PAY FREQUENCY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = onSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val frequencies = listOf(
                    "SEMI_MONTHLY" to "2x / Month",
                    "MONTHLY" to "Monthly",
                    "WEEKLY" to "Weekly"
                )
                frequencies.forEach { (freqKey, label) ->
                    val isSelected = settings.cutoffFrequency.equals(freqKey, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) onSurface else outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(
                                color = if (isSelected) GoldAccent else surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                onSaveSettings(settings.copy(cutoffFrequency = freqKey))
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isSelected) Color.Black else onSurface
                        )
                    }
                }
            }

            // ── Frequency-Specific Interactive Setup ─────────────────────────
            when (settings.cutoffFrequency.uppercase(Locale.US)) {
                "SEMI_MONTHLY" -> {
                    SemiMonthlyCutoffSection(
                        settings = settings,
                        onSaveSettings = onSaveSettings,
                        onOpenPicker = { target, title, allowEom, maxDays, currentVal ->
                            pickerTarget = target
                            pickerTitle = title
                            pickerAllowEom = allowEom
                            pickerMaxDays = maxDays
                            pickerSelectedDay = currentVal
                            showDayPickerDialog = true
                        }
                    )
                }

                "MONTHLY" -> {
                    MonthlyCutoffSection(
                        settings = settings,
                        onSaveSettings = onSaveSettings,
                        onOpenPicker = { target, title, allowEom, maxDays, currentVal ->
                            pickerTarget = target
                            pickerTitle = title
                            pickerAllowEom = allowEom
                            pickerMaxDays = maxDays
                            pickerSelectedDay = currentVal
                            showDayPickerDialog = true
                        }
                    )
                }

                "WEEKLY" -> {
                    WeeklyCutoffSection(
                        settings = settings,
                        onSaveSettings = onSaveSettings
                    )
                }
            }

            // ── Interactive Live Month Calendar Visualizer ───────────────────
            HorizontalDivider(color = outline.copy(alpha = 0.4f))

            CutoffCalendarVisualizer(settings = settings)
        }
    }

    // ── Day Picker Dialog ────────────────────────────────────────────────────
    if (showDayPickerDialog && pickerTarget != null) {
        DaySelectionDialog(
            title = pickerTitle,
            currentDay = pickerSelectedDay,
            maxDays = pickerMaxDays,
            allowEndOfMonth = pickerAllowEom,
            onDaySelected = { selected ->
                when (pickerTarget) {
                    "SEMI_1" -> onSaveSettings(settings.copy(cutoffFirstEndDay = selected))
                    "SEMI_2" -> onSaveSettings(settings.copy(cutoffSecondEndDay = selected))
                    "MONTHLY" -> onSaveSettings(settings.copy(cutoffMonthlyEndDay = selected))
                }
                showDayPickerDialog = false
            },
            onDismiss = { showDayPickerDialog = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEMI-MONTHLY SETUP SECTION
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SemiMonthlyCutoffSection(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onOpenPicker: (target: String, title: String, allowEom: Boolean, maxDays: Int, currentVal: Int) -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val isStandard15Eom = settings.cutoffFirstEndDay == 15 &&
            (settings.cutoffSecondEndDay == 0 || settings.cutoffSecondEndDay >= 30)
    val isBpo25And10 = settings.cutoffFirstEndDay == 25 && settings.cutoffSecondEndDay == 10
    val isCustom = !isStandard15Eom && !isBpo25And10

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "QUICK PRESETS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        // Preset Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preset 1: Standard
            CutoffPresetChip(
                label = "1st-15th & 16th-End",
                caption = "Standard Payroll",
                isSelected = isStandard15Eom,
                onClick = {
                    onSaveSettings(settings.copy(cutoffFirstEndDay = 15, cutoffSecondEndDay = 0))
                }
            )

            // Preset 2: BPO / 10th & 25th
            CutoffPresetChip(
                label = "11th-25th & 26th-10th",
                caption = "BPO / Mid-Month",
                isSelected = isBpo25And10,
                onClick = {
                    onSaveSettings(settings.copy(cutoffFirstEndDay = 25, cutoffSecondEndDay = 10))
                }
            )

            // Preset 3: Custom
            CutoffPresetChip(
                label = "Custom Days",
                caption = "${settings.cutoffFirstEndDay} & ${if (settings.cutoffSecondEndDay == 0) "EOM" else settings.cutoffSecondEndDay}",
                isSelected = isCustom,
                onClick = {
                    // Open picker directly for 1st day if switching to custom
                    onOpenPicker("SEMI_1", "Select 1st Cutoff End Day", false, 28, settings.cutoffFirstEndDay)
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Visual Cutoff End Day Selectors
        Text(
            text = "CUTOFF BOUNDARY DAYS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1st Cutoff End Button
            DaySelectorCard(
                modifier = Modifier.weight(1f),
                title = "1st Period End",
                displayValue = "Day ${settings.cutoffFirstEndDay}",
                subtitle = "Cycle 1 ends on day ${settings.cutoffFirstEndDay}",
                onClick = {
                    onOpenPicker("SEMI_1", "1st Cutoff End Day", false, 28, settings.cutoffFirstEndDay)
                }
            )

            // 2nd Cutoff End Button
            val secondText = if (settings.cutoffSecondEndDay == 0) "End of Month" else "Day ${settings.cutoffSecondEndDay}"
            DaySelectorCard(
                modifier = Modifier.weight(1f),
                title = "2nd Period End",
                displayValue = secondText,
                subtitle = if (settings.cutoffSecondEndDay == 0) "Last day of calendar" else "Cycle 2 ends on day ${settings.cutoffSecondEndDay}",
                onClick = {
                    onOpenPicker("SEMI_2", "2nd Cutoff End Day", true, 31, settings.cutoffSecondEndDay)
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MONTHLY SETUP SECTION
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthlyCutoffSection(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onOpenPicker: (target: String, title: String, allowEom: Boolean, maxDays: Int, currentVal: Int) -> Unit
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val presets = listOf(
        0 to "End of Month (Last Day)",
        15 to "15th of Month",
        20 to "20th of Month",
        25 to "25th of Month"
    )
    val isPreset = presets.any { it.first == settings.cutoffMonthlyEndDay }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "MONTHLY CUTOFF DAY PRESETS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { (day, label) ->
                val isSelected = settings.cutoffMonthlyEndDay == day
                CutoffPresetChip(
                    label = label,
                    isSelected = isSelected,
                    onClick = {
                        onSaveSettings(settings.copy(cutoffMonthlyEndDay = day))
                    }
                )
            }

            // Custom Option
            CutoffPresetChip(
                label = if (!isPreset) "Custom (Day ${settings.cutoffMonthlyEndDay})" else "Custom Day...",
                isSelected = !isPreset,
                onClick = {
                    onOpenPicker(
                        "MONTHLY",
                        "Monthly Cutoff End Day",
                        true,
                        31,
                        settings.cutoffMonthlyEndDay
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Visual Cutoff Card
        DaySelectorCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Monthly Cutoff Closes On",
            displayValue = if (settings.cutoffMonthlyEndDay == 0) "Last Day of Month (EOM)" else "Day ${settings.cutoffMonthlyEndDay} of Month",
            subtitle = "Tap to pick any calendar day from 1 to 31 or End of Month",
            onClick = {
                onOpenPicker(
                    "MONTHLY",
                    "Monthly Cutoff End Day",
                    true,
                    31,
                    settings.cutoffMonthlyEndDay
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WEEKLY SETUP SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WeeklyCutoffSection(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val daysOfWeek = listOf(
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat",
        Calendar.SUNDAY to "Sun"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "WEEK STARTS ON",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            daysOfWeek.forEach { (calDay, label) ->
                val isSelected = settings.cutoffWeeklyStartDay == calDay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) onSurface else outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = if (isSelected) GoldAccent else surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onSaveSettings(settings.copy(cutoffWeeklyStartDay = calDay))
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        color = if (isSelected) Color.Black else onSurface
                    )
                }
            }
        }

        val startLabel = daysOfWeek.firstOrNull { it.first == settings.cutoffWeeklyStartDay }?.second ?: "Mon"
        val endCalDay = if (settings.cutoffWeeklyStartDay == Calendar.SUNDAY) Calendar.SATURDAY else settings.cutoffWeeklyStartDay - 1
        val endLabel = daysOfWeek.firstOrNull { it.first == endCalDay }?.second ?: "Sun"

        Text(
            text = "7-day cycle runs from $startLabel morning to $endLabel midnight.",
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INTERACTIVE MINI-CALENDAR VISUALIZER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CutoffCalendarVisualizer(
    settings: AppSettings
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val now = remember { System.currentTimeMillis() }
    val currentCal = remember { Calendar.getInstance().apply { timeInMillis = now } }
    val currentYear = currentCal.get(Calendar.YEAR)
    val currentMonth = currentCal.get(Calendar.MONTH)
    val todayDay = currentCal.get(Calendar.DAY_OF_MONTH)

    // Calendar for 1st day of the month
    val monthCal = remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.
    val monthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCal.time) }

    val previewCurrent = CutoffHelper.getCurrentCutoff(now, settings)
    val previewPrev = CutoffHelper.getPreviousCutoff(now, settings)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Visualizer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$monthName Cycle Map",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
            }

            // Legend indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(GoldAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cycle 1",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(outline, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cycle 2",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = onSurfaceVariant
                    )
                }
            }
        }

        // Calendar Grid Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outline, RoundedCornerShape(12.dp))
                .background(surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Day of Week Header Row: S M T W T F S
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayHeader ->
                        Text(
                            text = dayHeader,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Days Matrix
                val totalCells = (firstDayOfWeek - 1) + daysInMonth
                val totalRows = (totalCells + 6) / 7

                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - (firstDayOfWeek - 1) + 1

                            if (dayNumber in 1..daysInMonth) {
                                val isToday = dayNumber == todayDay

                                // Determine cycle period color
                                val isPeriod1 = isDayInPeriod1(dayNumber, daysInMonth, settings)
                                val isCutoffEnd = isDayCutoffEnd(dayNumber, daysInMonth, settings)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isToday -> onSurface
                                                isPeriod1 -> GoldAccent.copy(alpha = 0.28f)
                                                else -> onSurface.copy(alpha = 0.08f)
                                            }
                                        )
                                        .border(
                                            width = when {
                                                isToday -> 2.dp
                                                isCutoffEnd -> 1.5.dp
                                                else -> 0.5.dp
                                            },
                                            color = when {
                                                isToday -> GoldAccent
                                                isCutoffEnd -> if (isPeriod1) GoldAccent else onSurface
                                                else -> Color.Transparent
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isToday || isCutoffEnd) FontWeight.Black else FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = when {
                                                isToday -> MaterialTheme.colorScheme.surface
                                                isPeriod1 -> onSurface
                                                else -> onSurface.copy(alpha = 0.8f)
                                            }
                                        )
                                        if (isCutoffEnd) {
                                            Box(
                                                modifier = Modifier
                                                    .size(3.dp)
                                                    .background(
                                                        if (isToday) GoldAccent else onSurface,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Active Cutoff Banner Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outline, RoundedCornerShape(12.dp))
                .background(surfaceVariant, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Cutoff Period",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceVariant
                    )
                    Text(
                        text = "${previewCurrent.daysRemaining} days left",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = onSurface
                    )
                }
                Text(
                    text = previewCurrent.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = onSurface
                )
                Text(
                    text = previewCurrent.paydayText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariant
                )

                HorizontalDivider(color = outline.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Previous Cutoff: ${previewPrev.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG & COMPONENT HELPERS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CutoffPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) onSurface else outline,
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                color = if (isSelected) GoldAccent else surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                color = if (isSelected) Color.Black else onSurface
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isSelected) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DaySelectorCard(
    title: String,
    displayValue: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .border(1.dp, outline, RoundedCornerShape(12.dp))
            .background(surfaceVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurface
                )
                Icon(
                    Icons.Default.EditCalendar,
                    contentDescription = null,
                    tint = onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = onSurfaceVariant
            )
        }
    }
}

/**
 * Modern tactile day-of-month picker dialog (grid of 1..maxDays + End of Month button).
 */
@Composable
private fun DaySelectionDialog(
    title: String,
    currentDay: Int,
    maxDays: Int,
    allowEndOfMonth: Boolean,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(2.dp, onSurface, RoundedCornerShape(16.dp))
                .background(surface, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = onSurface
                        )
                        Text(
                            text = "Tap a day to set as cycle cutoff",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = onSurface)
                    }
                }

                // End of Month Button if allowed
                if (allowEndOfMonth) {
                    val isEomSelected = currentDay == 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isEomSelected) 2.dp else 1.dp,
                                color = if (isEomSelected) onSurface else outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(
                                color = if (isEomSelected) GoldAccent else surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onDaySelected(0) }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = if (isEomSelected) Color.Black else onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "End of the Month (Last Day)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isEomSelected) Color.Black else onSurface
                            )
                        }
                    }
                }

                // Grid of 1..maxDays
                Text(
                    text = "SPECIFIC CALENDAR DAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                val rows = (maxDays + 6) / 7
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (c in 0 until 7) {
                                val dayNum = r * 7 + c + 1
                                if (dayNum <= maxDays) {
                                    val isSelected = currentDay == dayNum
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) onSurface else outline,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .background(
                                                color = if (isSelected) GoldAccent else surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onDaySelected(dayNum) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) Color.Black else onSurface
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = onSurface)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PERIOD CALCULATION LOGIC FOR CALENDAR
// ─────────────────────────────────────────────────────────────────────────────
private fun isDayInPeriod1(day: Int, maxDays: Int, settings: AppSettings): Boolean {
    return when (settings.cutoffFrequency.uppercase(Locale.US)) {
        "MONTHLY" -> {
            val endDay = if (settings.cutoffMonthlyEndDay in 1..31) settings.cutoffMonthlyEndDay else maxDays
            day <= endDay
        }
        "WEEKLY" -> {
            // Alternate weeks in month
            ((day - 1) / 7) % 2 == 0
        }
        else -> {
            // Semi-monthly
            val firstEnd = if (settings.cutoffFirstEndDay in 1..28) settings.cutoffFirstEndDay else 15
            val secondEnd = if (settings.cutoffSecondEndDay in 1..31) settings.cutoffSecondEndDay else maxDays
            val isStandard = firstEnd < secondEnd

            if (isStandard) {
                day <= firstEnd
            } else {
                // Cross-month e.g. 25 & 10: Period A is 11..25
                day in (secondEnd + 1)..firstEnd
            }
        }
    }
}

private fun isDayCutoffEnd(day: Int, maxDays: Int, settings: AppSettings): Boolean {
    return when (settings.cutoffFrequency.uppercase(Locale.US)) {
        "MONTHLY" -> {
            val endDay = if (settings.cutoffMonthlyEndDay in 1..31) settings.cutoffMonthlyEndDay else maxDays
            day == endDay
        }
        "WEEKLY" -> false
        else -> {
            val firstEnd = if (settings.cutoffFirstEndDay in 1..28) settings.cutoffFirstEndDay else 15
            val secondEnd = if (settings.cutoffSecondEndDay == 0 || settings.cutoffSecondEndDay > maxDays) maxDays else settings.cutoffSecondEndDay
            day == firstEnd || day == secondEnd
        }
    }
}
