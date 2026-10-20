package com.example.ottracker.ui.components

import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.OvertimeRecord
import com.example.ottracker.data.OvertimeType
import com.example.ottracker.theme.DangerRed
import com.example.ottracker.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class QuickPresetItem(
    val hour: Int,
    val minute: Int,
    val label: String,
    val header: String
)

// Colors for category cards
private val SpecialHolidayColor = Color(0xFF6366F1)  // Indigo
private val RegularHolidayColor = Color(0xFFF59E0B)  // Amber/Gold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeRangeOvertimeDialog(
    dateMillis: Long,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (OvertimeRecord) -> Unit,
    modifier: Modifier = Modifier,
    existingRecord: OvertimeRecord? = null,
    onDelete: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Reuse one Calendar to extract existing record times instead of allocating 4 separate ones
    val existingCal = existingRecord?.let { Calendar.getInstance().apply { timeInMillis = it.startTimeMillis } }

    val initStartHour = existingCal?.get(Calendar.HOUR_OF_DAY) ?: settings.defaultStartHour
    val initStartMin = existingCal?.get(Calendar.MINUTE) ?: settings.defaultStartMinute

    val initEndHour = existingRecord?.let {
        existingCal!!.timeInMillis = it.endTimeMillis
        existingCal.get(Calendar.HOUR_OF_DAY)
    } ?: 18

    val initEndMin = existingRecord?.let {
        existingCal!!.get(Calendar.MINUTE)
    } ?: 0

    var startHour by remember { mutableIntStateOf(initStartHour) }
    var startMinute by remember { mutableIntStateOf(initStartMin) }
    var endHour by remember { mutableIntStateOf(initEndHour) }
    var endMinute by remember { mutableIntStateOf(initEndMin) }
    var selectedOtType by remember { mutableStateOf(existingRecord?.overtimeType ?: OvertimeType.REGULAR) }

    val startCal = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, startHour)
        set(Calendar.MINUTE, startMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCal = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, endHour)
        set(Calendar.MINUTE, endMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= startCal.timeInMillis) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val tempRecord = remember(startCal.timeInMillis, endCal.timeInMillis, selectedOtType, settings.baseHourlyRate) {
        OvertimeRecord(
            dateMillis = dateMillis,
            startTimeMillis = startCal.timeInMillis,
            endTimeMillis = endCal.timeInMillis,
            hourlyRate = settings.baseHourlyRate,
            overtimeType = selectedOtType
        )
    }
    val durationMinutes = tempRecord.durationMinutes
    val durationHours = tempRecord.durationHours
    val effectiveRate = tempRecord.effectiveDayRate
    val moneyMade = tempRecord.totalPay

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()) }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .border(1.dp, outline, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── HEADER ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (existingRecord != null) "Edit Overtime" else "Add Overtime",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(dateMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant
                        )
                    }
                    NeoIconButton(onClick = onDismiss, size = 36.dp) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = outline,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // ── SECTION LABEL ─────────────────────────────────────────────
                Text(
                    text = "OVERTIME HOURS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // ── START / END TIME CARDS ────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Time
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, outline, RoundedCornerShape(14.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        startHour = h
                                        startMinute = m
                                    },
                                    startHour,
                                    startMinute,
                                    false
                                ).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "START TIME",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = timeFormat.format(startCal.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = onSurface
                            )
                        }
                    }

                    // End Time
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, outline, RoundedCornerShape(14.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        endHour = h
                                        endMinute = m
                                    },
                                    endHour,
                                    endMinute,
                                    false
                                ).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "END TIME",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = timeFormat.format(endCal.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── QUICK PRESETS ─────────────────────────────────────────────
                Text(
                    "QUICK PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val groupedPresets = remember(startHour, startMinute) {
                    val startTotalMin = startHour * 60 + startMinute
                    val minUntilMidnight = ((24 * 60) - startTotalMin).let {
                        if (it in 30..(12 * 60)) it else (7 * 60)
                    }
                    val stepCount = minUntilMidnight / 30
                    val presetOffsets = (1..stepCount).map { it * 0.5 }
                    val presetTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val hourHeaderFormat = SimpleDateFormat("h a", Locale.getDefault())
                    val cal = Calendar.getInstance()
                    presetOffsets.map { hoursOffset ->
                        val totalMinutes = startTotalMin + (hoursOffset * 60).toInt()
                        val h = (totalMinutes / 60) % 24
                        val m = totalMinutes % 60
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, m)
                        val timeLabel = presetTimeFormat.format(cal.time)
                        val hoursLabel = if (hoursOffset == hoursOffset.toLong().toDouble()) "${hoursOffset.toInt()}h" else "${hoursOffset}h"
                        val groupHeader = if (h == 0 && m == 0) "12 AM" else hourHeaderFormat.format(cal.time)
                        QuickPresetItem(
                            hour = h,
                            minute = m,
                            label = "$timeLabel ($hoursLabel)",
                            header = groupHeader
                        )
                    }.groupBy { it.header }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    groupedPresets.forEach { (header, items) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Hour Header Badge (e.g., 5 PM, 6 PM)
                            Surface(
                                color = surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .width(48.dp)
                                    .border(1.dp, outline, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items.forEach { item ->
                                    val isActive = endHour == item.hour && endMinute == item.minute
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isActive) onSurface else surfaceVariant)
                                            .border(
                                                width = 1.dp,
                                                color = if (isActive) onSurface else outline,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                endHour = item.hour
                                                endMinute = item.minute
                                            }
                                            .padding(horizontal = 9.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = item.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isActive) MaterialTheme.colorScheme.background else onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = outline,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // ── OVERTIME CATEGORY / RATE TYPE (3-CARD SELECTOR) ───────────
                Text(
                    text = "RATE CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 3-card DOLE rate category selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card data: type, emoji, short label, multiplier label, accent color
                    data class CategoryCard(
                        val type: String,
                        val emoji: String,
                        val title: String,
                        val multiplierLabel: String,
                        val accentColor: Color
                    )

                    val categories = listOf(
                        CategoryCard(OvertimeType.REGULAR, "⚡", "REGULAR\nOT", "×1.00", onSurface),
                        CategoryCard(OvertimeType.SPECIAL_HOLIDAY, "🏖️", "SPECIAL\nHOLIDAY", "×1.30 (+30%)", SpecialHolidayColor),
                        CategoryCard(OvertimeType.REGULAR_HOLIDAY, "🎉", "REGULAR\nHOLIDAY", "×2.00 (+100%)", RegularHolidayColor)
                    )

                    categories.forEach { cat ->
                        val isSelected = selectedOtType == cat.type
                        val computedRate = settings.baseHourlyRate * OvertimeType.multiplierFor(cat.type)

                        NeoCard(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (selectedOtType != cat.type) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedOtType = cat.type
                                }
                            },
                            containerColor = if (isSelected) {
                                if (cat.type == OvertimeType.REGULAR) onSurface
                                else cat.accentColor
                            } else surfaceVariant,
                            borderColor = if (isSelected) {
                                if (cat.type == OvertimeType.REGULAR) onSurface else Color.Black
                            } else outline,
                            shadowColor = if (isSelected) {
                                if (cat.type == OvertimeType.REGULAR) GoldAccent else Color.Black
                            } else outline,
                            shadowOffset = if (isSelected) 3.dp else 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${cat.emoji} ${cat.title}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    color = when {
                                        isSelected && cat.type == OvertimeType.REGULAR -> MaterialTheme.colorScheme.background
                                        isSelected && cat.type == OvertimeType.REGULAR_HOLIDAY -> Color.Black
                                        isSelected -> Color.White
                                        else -> onSurface
                                    }
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = cat.multiplierLabel,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.5.sp,
                                    maxLines = 1,
                                    color = when {
                                        isSelected && cat.type == OvertimeType.REGULAR -> MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                        isSelected && cat.type == OvertimeType.REGULAR_HOLIDAY -> Color.Black.copy(alpha = 0.8f)
                                        isSelected -> Color.White.copy(alpha = 0.8f)
                                        else -> onSurfaceVariant
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${settings.currencySymbol}${String.format(Locale.US, "%.2f", computedRate)}/h",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    color = when {
                                        isSelected && cat.type == OvertimeType.REGULAR -> GoldAccent
                                        isSelected && cat.type == OvertimeType.REGULAR_HOLIDAY -> Color.Black.copy(alpha = 0.9f)
                                        isSelected -> GoldAccent
                                        else -> cat.accentColor
                                    }
                                )
                            }
                        }
                    }
                }

                // Rate formula preview below category cards
                val multiplier = OvertimeType.multiplierFor(selectedOtType)
                if (selectedOtType != OvertimeType.REGULAR) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (selectedOtType == OvertimeType.SPECIAL_HOLIDAY) Color(0xFF1E1B4B) else Color(0xFF1C1917),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (selectedOtType == OvertimeType.SPECIAL_HOLIDAY) SpecialHolidayColor.copy(alpha = 0.3f) else RegularHolidayColor.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = "Rate: ${settings.currencySymbol}${String.format(Locale.US, "%.2f", settings.baseHourlyRate)} × ${String.format(Locale.US, "%.2f", multiplier)} = ${settings.currencySymbol}${String.format(Locale.US, "%.2f", effectiveRate)}/hr",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = outline,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // ── EARNINGS PREVIEW CARD ─────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, onSurface, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "OVERTIME EARNINGS PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                val hrs = durationMinutes / 60
                                val mins = durationMinutes % 60
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", durationHours)} hrs",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    color = onSurface
                                )
                                Text(
                                    text = if (mins > 0) "${hrs}h ${mins}m" else "${hrs} hour${if (hrs > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    color = onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f, fill = false),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", moneyMade)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    color = GoldAccent
                                )
                                val rateSubtext = when {
                                    selectedOtType != OvertimeType.REGULAR ->
                                        "@ ${settings.currencySymbol}${String.format(Locale.US, "%.2f", effectiveRate)}/h (${OvertimeType.shortLabelFor(selectedOtType)})"
                                    else ->
                                        "@ ${settings.currencySymbol}${String.format(Locale.US, "%.2f", effectiveRate)}/hr"
                                }
                                val subtextColor = when (selectedOtType) {
                                    OvertimeType.SPECIAL_HOLIDAY -> SpecialHolidayColor
                                    OvertimeType.REGULAR_HOLIDAY -> RegularHolidayColor
                                    else -> onSurfaceVariant
                                }
                                Text(
                                    text = rateSubtext,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (selectedOtType != OvertimeType.REGULAR) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    color = subtextColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── ACTIONS ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (existingRecord != null && onDelete != null) {
                        NeoIconButton(
                            onClick = {
                                onDelete(existingRecord.id)
                                onDismiss()
                            },
                            size = 38.dp,
                            shadowColor = DangerRed,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row {
                        NeoButton(
                            onClick = onDismiss,
                            containerColor = surfaceVariant,
                            contentColor = onSurfaceVariant,
                            borderColor = outline,
                            shadowColor = outline,
                            shadowOffset = 2.dp
                        ) {
                            Text(
                                "Cancel",
                                color = onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        NeoButton(
                            onClick = {
                                val record = OvertimeRecord(
                                    id = existingRecord?.id ?: java.util.UUID.randomUUID().toString(),
                                    dateMillis = dateMillis,
                                    startTimeMillis = startCal.timeInMillis,
                                    endTimeMillis = endCal.timeInMillis,
                                    hourlyRate = settings.baseHourlyRate,
                                    overtimeType = selectedOtType,
                                    notes = existingRecord?.notes ?: ""
                                )
                                onSave(record)
                                onDismiss()
                            },
                            containerColor = onSurface,
                            contentColor = MaterialTheme.colorScheme.background,
                            shadowColor = GoldAccent,
                            shadowOffset = 3.dp
                        ) {
                            Text(
                                "Save Overtime",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.background
                            )
                        }
                    }
                }
            }
        }
    }
}
