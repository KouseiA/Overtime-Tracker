package com.example.ottracker.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.example.ottracker.theme.GoldAccent
import com.example.ottracker.ui.components.NeoCard
import com.example.ottracker.ui.components.TimeRangeOvertimeDialog
import com.example.ottracker.ui.main.DashboardStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun OtFormTab(
    records: List<OvertimeRecord>,
    stats: DashboardStats,
    settings: AppSettings,
    onAddOrUpdateRecord: (OvertimeRecord) -> Unit = {},
    onDeleteRecord: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var recordToEdit by remember { mutableStateOf<OvertimeRecord?>(null) }

    if (recordToEdit != null) {
        TimeRangeOvertimeDialog(
            dateMillis = recordToEdit!!.dateMillis,
            existingRecord = recordToEdit,
            settings = settings,
            onDismiss = { recordToEdit = null },
            onSave = { record ->
                onAddOrUpdateRecord(record)
                recordToEdit = null
            },
            onDelete = { id ->
                onDeleteRecord(id)
                recordToEdit = null
            }
        )
    }

    // 0 = Current Cutoff, 1 = Previous Cutoff, 2 = All Records
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val filteredRecords = remember(records, selectedFilterIndex, settings) {
        val nowMillis = System.currentTimeMillis()
        when (selectedFilterIndex) {
            0 -> { // Current Cutoff
                val currentPeriod = com.example.ottracker.data.CutoffHelper.getCurrentCutoff(nowMillis, settings)
                records.filter { rec ->
                    rec.startTimeMillis in currentPeriod.startMillis..currentPeriod.endMillis
                }.sortedBy { it.startTimeMillis }
            }
            1 -> { // Previous Cutoff
                val prevPeriod = com.example.ottracker.data.CutoffHelper.getPreviousCutoff(nowMillis, settings)
                records.filter { rec ->
                    rec.startTimeMillis in prevPeriod.startMillis..prevPeriod.endMillis
                }.sortedBy { it.startTimeMillis }
            }
            else -> records.sortedBy { it.startTimeMillis }
        }
    }

    val totalHours = filteredRecords.sumOf { it.durationHours }
    val totalDayHours = filteredRecords.sumOf { it.dayOtHours }
    val totalNightHours = filteredRecords.sumOf { it.nightOtHours }
    val totalPay = filteredRecords.sumOf { it.totalPay }

    val cutoffTitle = when (selectedFilterIndex) {
        0 -> stats.currentCutoff.title
        1 -> stats.previousCutoff.title
        else -> "All Logged Shifts"
    }

    val paydayText = when (selectedFilterIndex) {
        0 -> stats.currentCutoff.payday
        1 -> stats.previousCutoff.payday
        else -> "N/A"
    }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val monthDayFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val bg = MaterialTheme.colorScheme.background

    LazyColumn(
        modifier = modifier.padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = onSurface,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Official Overtime Record Sheet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    Text(
                        text = "Formal DTR table for transcribing onto paper OT form",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }
            }
        }

        // ── CUTOFF FILTER CHIPS (Scrollable to support any phone width or large system fonts) ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "This Cutoff (${stats.currentCutoff.title})",
                    "Previous Cutoff",
                    "All"
                )
                filters.forEachIndexed { index, label ->
                    val isSelected = selectedFilterIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterIndex = index },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = onSurface,
                            selectedLabelColor = bg,
                            containerColor = surfaceVariant,
                            labelColor = onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = outline,
                            selectedBorderColor = onSurface
                        )
                    )
                }
            }
        }

        // ── FORMAL DTR DOCUMENT HEADER CARD ──────────────────────────────────
        item {
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OVERTIME ATTENDANCE SHEET",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = onSurfaceVariant,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                "CUTOFF PERIOD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cutoffTitle,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "PAYROLL DATE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = paydayText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = outline)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Responsive 2-column layout to prevent text squishing on narrow screens / custom fonts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "STANDARD SHIFT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "8:00 AM – 5:00 PM",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RATE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${settings.currencySymbol}${String.format(Locale.US, "%.2f", settings.baseHourlyRate)}/HR",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = onSurface
                            )
                        }
                    }
                }
            }
        }

        // ── UNIFIED HIGH-CONTRAST MONOCHROME TABLE (100% Screen Width, Zero Horizontal Scroll) ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, onSurface, RoundedCornerShape(14.dp))
            ) {
                // TABLE HEADER ROW (Fits 100% of screen width cleanly)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(onSurface)
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DATE",
                        modifier = Modifier.weight(1.05f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = bg
                    )
                    Text(
                        text = "TIME IN",
                        modifier = Modifier.weight(1.0f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = bg
                    )
                    Text(
                        text = "TIME OUT",
                        modifier = Modifier.weight(1.0f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = bg
                    )
                    Text(
                        text = "HOURS",
                        modifier = Modifier.weight(0.85f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = bg
                    )
                    Text(
                        text = "AMOUNT",
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(end = 4.dp),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = bg
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = outline)

                if (filteredRecords.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surface)
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "NO OT SHIFTS RECORDED",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No overtime logged for this cutoff period. Tap Calendar to add your shifts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    filteredRecords.forEachIndexed { index, record ->
                        val dateObj = Date(record.dateMillis)
                        val monthDay = monthDayFormat.format(dateObj)
                        val dayOfWeek = dayOfWeekFormat.format(dateObj)
                        val timeIn = timeFormat.format(Date(record.startTimeMillis))
                        val timeOut = timeFormat.format(Date(record.endTimeMillis))
                        val rowBg = if (index % 2 == 0) surface else surfaceVariant

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    recordToEdit = record
                                }
                                .height(IntrinsicSize.Min)
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Date & Day of Week
                            Column(
                                modifier = Modifier
                                    .weight(1.05f)
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = monthDay,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    color = onSurface
                                )
                                Text(
                                    text = "($dayOfWeek)",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    color = onSurfaceVariant
                                )
                                if (record.isHoliday) {
                                    Text(
                                        text = "HOLIDAY",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.5.sp,
                                        maxLines = 1,
                                        color = GoldAccent
                                    )
                                }
                            }

                            VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = outline)

                            // Column 2: TIME IN (e.g. 5:00 PM)
                            Text(
                                text = timeIn,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .padding(horizontal = 2.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = onSurface
                            )

                            VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = outline)

                            // Column 3: TIME OUT (e.g. 8:00 PM)
                            Text(
                                text = timeOut,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .padding(horizontal = 2.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = onSurface
                            )

                            VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = outline)

                            // Column 4: TOTAL HOURS (e.g. 3.00h)
                            Column(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", record.durationHours)}h",
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = onSurface
                                )
                                if (record.hasNightDifferential) {
                                    Text(
                                        text = "🌙 ${String.format(Locale.US, "%.1f", record.nightOtHours)}h",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.5.sp,
                                        maxLines = 1,
                                        color = Color(0xFFA5B4FC)
                                    )
                                }
                            }

                            VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = outline)

                            // Column 5: AMOUNT (e.g. ₱281.25)
                            Column(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .padding(end = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${settings.currencySymbol}${String.format(Locale.US, "%.2f", record.totalPay)}",
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    color = GoldAccent
                                )
                                if (record.isHoliday) {
                                    Text(
                                        text = "+30% OT",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.5.sp,
                                        maxLines = 1,
                                        color = GoldAccent
                                    )
                                } else if (record.hasNightDifferential) {
                                    Text(
                                        text = "+20% NSD",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.5.sp,
                                        maxLines = 1,
                                        color = Color(0xFFA5B4FC)
                                    )
                                }
                            }
                        }

                        // Divider between rows
                        if (index < filteredRecords.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = outline)
                        }
                    }

                    // ── INTEGRATED TABLE TOTAL ROW (Authentic DTR Ledger Footer) ──
                    HorizontalDivider(thickness = 1.5.dp, color = onSurface)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceVariant)
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL",
                            modifier = Modifier
                                .weight(3.05f)
                                .padding(start = 10.dp),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.5.sp,
                            letterSpacing = 1.sp,
                            color = onSurface
                        )

                        VerticalDivider(modifier = Modifier.height(18.dp), thickness = 1.dp, color = outline)

                        Text(
                            text = "${String.format(Locale.US, "%.2f", totalHours)}h",
                            modifier = Modifier
                                .weight(0.85f)
                                .padding(horizontal = 2.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp,
                            color = onSurface
                        )

                        VerticalDivider(modifier = Modifier.height(18.dp), thickness = 1.dp, color = outline)

                        Text(
                            text = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", totalPay)}",
                            modifier = Modifier
                                .weight(1.1f)
                                .padding(end = 4.dp),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp,
                            color = GoldAccent
                        )
                    }
                }
            }

            if (filteredRecords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap any row above to edit hours or delete shift",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // ── FORMAL TOTAL SUMMARY FOOTER CARD ─────────────────────────────────
        if (filteredRecords.isNotEmpty()) {
            item {
                NeoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "TOTAL CUTOFF SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL OVERTIME HOURS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", totalHours)} hrs",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = onSurface
                                )
                                if (totalNightHours > 0.0) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", totalDayHours)}h Day · ${String.format(Locale.US, "%.1f", totalNightHours)}h Night (+20%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA5B4FC)
                                    )
                                }
                                Text(
                                    text = "${filteredRecords.size} Total Shifts Logged",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TOTAL OVERTIME PAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", totalPay)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                                Text(
                                    text = "@ ${settings.currencySymbol}${String.format(Locale.US, "%.2f", settings.baseHourlyRate)}/hr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
