package com.example.ottracker.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.OvertimeRecord
import com.example.ottracker.data.OvertimeType
import com.example.ottracker.theme.DangerRed
import com.example.ottracker.theme.GoldAccent
import androidx.compose.ui.text.font.FontFamily
import com.example.ottracker.ui.components.NeoButton
import com.example.ottracker.ui.components.NeoCard
import com.example.ottracker.ui.components.NeoIconButton
import com.example.ottracker.ui.components.OvertimeCategoryStamp
import com.example.ottracker.ui.components.TimeRangeOvertimeDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarTab(
    records: List<OvertimeRecord>,
    settings: AppSettings,
    onAddOrUpdateRecord: (OvertimeRecord) -> Unit,
    onDeleteRecord: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val todayCal = Calendar.getInstance()
    var calendarYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }
    var selectedDayOfMonth by remember { mutableStateOf<Int?>(todayCal.get(Calendar.DAY_OF_MONTH)) }

    var showDialogForDateMillis by remember { mutableStateOf<Long?>(null) }
    var recordToEdit by remember { mutableStateOf<OvertimeRecord?>(null) }

    if (showDialogForDateMillis != null) {
        TimeRangeOvertimeDialog(
            dateMillis = showDialogForDateMillis!!,
            existingRecord = recordToEdit,
            settings = settings,
            onDismiss = {
                showDialogForDateMillis = null
                recordToEdit = null
            },
            onSave = { record ->
                onAddOrUpdateRecord(record)
                showDialogForDateMillis = null
                recordToEdit = null
            },
            onDelete = { id ->
                onDeleteRecord(id)
                showDialogForDateMillis = null
                recordToEdit = null
            }
        )
    }

    val currentMonthCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, calendarYear)
        set(Calendar.MONTH, calendarMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val daysInMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = currentMonthCal.get(Calendar.DAY_OF_WEEK)

    // ALL CAPS month title for monochrome look
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val selectedDayFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    val monthTitle = monthFormat.format(currentMonthCal.time).uppercase()
    val monthTitleReadable = monthFormat.format(currentMonthCal.time)

    val recordsByDay = remember(records, calendarYear, calendarMonth) {
        val map = mutableMapOf<Int, MutableList<OvertimeRecord>>()
        val recCal = Calendar.getInstance()
        for (rec in records) {
            recCal.timeInMillis = rec.startTimeMillis
            if (recCal.get(Calendar.YEAR) == calendarYear && recCal.get(Calendar.MONTH) == calendarMonth) {
                val day = recCal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(rec)
            }
        }
        map
    }

    val displayedRecords = remember(records, calendarYear, calendarMonth, selectedDayOfMonth) {
        val recCal = Calendar.getInstance()
        records.filter { rec ->
            recCal.timeInMillis = rec.startTimeMillis
            val matchYear = recCal.get(Calendar.YEAR) == calendarYear
            val matchMonth = recCal.get(Calendar.MONTH) == calendarMonth
            if (selectedDayOfMonth != null) {
                matchYear && matchMonth && recCal.get(Calendar.DAY_OF_MONTH) == selectedDayOfMonth
            } else {
                matchYear && matchMonth
            }
        }
    }

    val totalHours = remember(displayedRecords) { displayedRecords.sumOf { it.durationHours } }
    val totalPay = remember(displayedRecords) { displayedRecords.sumOf { it.totalPay } }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ── CALENDAR CARD ────────────────────────────────────────────────────
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(16.dp),
                borderColor = onSurface,
                containerColor = surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeoIconButton(
                            onClick = {
                                if (calendarMonth == 0) {
                                    calendarMonth = 11
                                    calendarYear -= 1
                                } else {
                                    calendarMonth -= 1
                                }
                                selectedDayOfMonth = null
                            },
                            size = 38.dp
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = onSurface
                        )

                        NeoIconButton(
                            onClick = {
                                if (calendarMonth == 11) {
                                    calendarMonth = 0
                                    calendarYear += 1
                                } else {
                                    calendarMonth += 1
                                }
                                selectedDayOfMonth = null
                            },
                            size = 38.dp
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = outline, modifier = Modifier.padding(vertical = 8.dp))

                    // Day of Week Labels — ALL CAPS
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                        dayNames.forEach { name ->
                            Text(
                                text = name,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1,
                                color = onSurfaceVariant,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Month Grid
                    val totalSlots = ((firstDayOfWeek - 1 + daysInMonth + 6) / 7) * 7
                    val rows = totalSlots / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0 until 7) {
                                val slotIndex = r * 7 + c
                                val dayNum = slotIndex - (firstDayOfWeek - 1) + 1

                                if (dayNum in 1..daysInMonth) {
                                    val isSelected = selectedDayOfMonth == dayNum
                                    val dayRecords = recordsByDay[dayNum]
                                    val hasRecord = !dayRecords.isNullOrEmpty()
                                    val dayTotalHours = dayRecords?.sumOf { it.durationHours } ?: 0.0

                                    val isToday = todayCal.get(Calendar.YEAR) == calendarYear &&
                                            todayCal.get(Calendar.MONTH) == calendarMonth &&
                                            todayCal.get(Calendar.DAY_OF_MONTH) == dayNum

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> onSurface
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .then(
                                                if (isToday && !isSelected)
                                                    Modifier.border(2.dp, onSurface, RoundedCornerShape(10.dp))
                                                else Modifier
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedDayOfMonth = dayNum
                                                val clickCal = Calendar.getInstance().apply {
                                                    set(calendarYear, calendarMonth, dayNum, 17, 0, 0)
                                                }
                                                showDialogForDateMillis = clickCal.timeInMillis
                                                recordToEdit = dayRecords?.firstOrNull()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = when {
                                                    isSelected || isToday -> FontWeight.Black
                                                    hasRecord -> FontWeight.Bold
                                                    else -> FontWeight.Normal
                                                },
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.background
                                                    else -> onSurface
                                                }
                                            )
                                            if (hasRecord) {
                                                val hasHoliday = dayRecords.any { it.isHoliday }
                                                Text(
                                                    text = "${String.format(Locale.US, "%.1f", dayTotalHours)}h${if (hasHoliday) "★" else ""}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.background.copy(alpha = 0.85f) else GoldAccent
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        if (r < rows - 1) Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        // ── SELECTED DATE SUMMARY BAR ────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, outline, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val headerText = if (selectedDayOfMonth != null) {
                            val selCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, calendarYear)
                                set(Calendar.MONTH, calendarMonth)
                                set(Calendar.DAY_OF_MONTH, selectedDayOfMonth!!)
                            }
                            selectedDayFormat.format(selCal.time)
                        } else {
                            "All of $monthTitleReadable"
                        }
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${String.format(Locale.US, "%.2f", totalHours)} hrs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                maxLines = 1,
                                color = onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("·", color = onSurfaceVariant, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${settings.currencySymbol}${String.format(Locale.US, "%.2f", totalPay)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                maxLines = 1,
                                color = GoldAccent
                            )
                        }
                    }

                    if (selectedDayOfMonth != null) {
                        val hasRecord = !recordsByDay[selectedDayOfMonth!!].isNullOrEmpty()
                        NeoButton(
                            onClick = {
                                val clickCal = Calendar.getInstance().apply {
                                    set(calendarYear, calendarMonth, selectedDayOfMonth!!, 17, 0, 0)
                                }
                                showDialogForDateMillis = clickCal.timeInMillis
                                recordToEdit = recordsByDay[selectedDayOfMonth!!]?.firstOrNull()
                            },
                            containerColor = onSurface,
                            contentColor = MaterialTheme.colorScheme.background,
                            shadowColor = GoldAccent,
                            shadowOffset = 2.5.dp
                        ) {
                            Icon(
                                if (hasRecord) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.background
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (hasRecord) "Edit OT" else "Add OT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.background
                            )
                        }
                    }
                }
            }
        }

        // ── RECORDS LIST ─────────────────────────────────────────────────────
        if (displayedRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, outline, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (selectedDayOfMonth != null)
                                "No overtime on this day"
                            else
                                "No overtime in $monthTitleReadable",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the date above to log your OT hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = displayedRecords,
                key = { it.id },
                contentType = { "overtime_record" }
            ) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, outline, RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showDialogForDateMillis = record.dateMillis
                            recordToEdit = record
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = record.formattedDate,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OvertimeCategoryStamp(
                                    overtimeType = record.overtimeType,
                                    rotation = -1.5f
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${record.formattedTimeRange}  ·  ${record.formattedHoursDecimal}${if (record.overtimeType != OvertimeType.REGULAR) "  ·  @ ${settings.currencySymbol}${String.format(Locale.US, "%.2f", record.effectiveHourlyRate)}/hr" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${settings.currencySymbol}${String.format(Locale.US, "%.2f", record.totalPay)}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldAccent
                            )
                            NeoIconButton(
                                onClick = { onDeleteRecord(record.id) },
                                size = 30.dp,
                                shadowOffset = 2.dp,
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = DangerRed.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
