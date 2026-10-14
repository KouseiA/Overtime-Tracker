package com.example.ottracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ottracker.theme.GoldAccent
import com.example.ottracker.ui.main.ChartBarData
import java.util.Locale

@Composable
fun EarningsBarChart(
    cutoffTrends: List<ChartBarData>,
    monthlyTrends: List<ChartBarData>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val haptic = LocalHapticFeedback.current

    // 0 = Cutoff Periods, 1 = Monthly
    var selectedMode by remember { mutableIntStateOf(0) }
    val currentList = if (selectedMode == 0) cutoffTrends else monthlyTrends

    // Default selected bar to the current active period (or the last bar)
    var selectedIndex by remember(selectedMode, currentList) {
        val currIdx = currentList.indexOfFirst { it.isCurrent }
        mutableIntStateOf(if (currIdx >= 0) currIdx else currentList.lastIndex)
    }

    val maxPay = (currentList.maxOfOrNull { it.pay } ?: 0.0).coerceAtLeast(1.0)
    val totalEarnings = currentList.sumOf { it.pay }
    val averageEarnings = if (currentList.isNotEmpty()) totalEarnings / currentList.size else 0.0
    val peakEarnings = currentList.maxOfOrNull { it.pay } ?: 0.0

    val selectedBar = currentList.getOrNull(selectedIndex)

    NeoCard(
        modifier = modifier.fillMaxWidth(),
        shadowOffset = 4.dp,
        shape = RoundedCornerShape(16.dp),
        borderColor = onSurface,
        containerColor = surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // ── HEADER ROW ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "EARNINGS TRENDS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = onSurfaceVariant
                        )
                        Text(
                            text = if (selectedMode == 0) "Last 6 Cutoff Periods" else "Last 6 Calendar Months",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))

                // Neo-Brutal Mode Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, onSurface, RoundedCornerShape(8.dp))
                        .background(surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                if (selectedMode != 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedMode = 0
                                }
                            }
                            .background(if (selectedMode == 0) onSurface else surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CUTOFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (selectedMode == 0) MaterialTheme.colorScheme.background else onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable {
                                if (selectedMode != 1) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedMode = 1
                                }
                            }
                            .background(if (selectedMode == 1) onSurface else surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "MONTH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (selectedMode == 1) MaterialTheme.colorScheme.background else onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── ACTIVE BAR DETAIL CALLOUT BANNER ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, outline, RoundedCornerShape(10.dp))
                    .background(surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (selectedBar != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedBar.fullTitle.uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", selectedBar.hours)} hrs rendered",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", selectedBar.pay)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (selectedBar.isCurrent) GoldAccent else onSurface
                        )
                    }
                } else {
                    Text(
                        text = "Tap any bar to inspect overtime payout",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── BARS CONTAINER ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                currentList.forEachIndexed { index, bar ->
                    val isSelected = selectedIndex == index
                    val fraction = if (maxPay > 0 && bar.pay > 0) {
                        (bar.pay / maxPay).toFloat().coerceIn(0.08f, 1.0f)
                    } else {
                        0.06f
                    }

                    val animatedFraction by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(durationMillis = 350),
                        label = "bar_anim_${bar.id}"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedIndex = index
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Top amount indicator
                        Text(
                            text = if (bar.pay > 0) {
                                if (bar.pay >= 10000) "${String.format(Locale.US, "%.1f", bar.pay / 1000)}k"
                                else "${String.format(Locale.US, "%,.0f", bar.pay)}"
                            } else "—",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected || bar.isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = when {
                                bar.isCurrent -> GoldAccent
                                isSelected -> onSurface
                                else -> onSurfaceVariant
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar Pillar Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // The colored column
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (isSelected) 0.88f else 0.78f)
                                    .fillMaxHeight(animatedFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        when {
                                            bar.isCurrent -> GoldAccent
                                            bar.pay > 0 -> onSurface
                                            else -> surfaceVariant
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected && !bar.isCurrent) GoldAccent else onSurface,
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Bottom period label
                        Text(
                            text = bar.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected || bar.isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) onSurface else onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = bar.subLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = outline)
            Spacer(modifier = Modifier.height(10.dp))

            // ── FOOTER STATS & LEGEND ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Current",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(onSurface)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Past",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceVariant
                    )
                }

                Row {
                    Text(
                        text = "Peak: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.0f", peakEarnings)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Avg: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.0f", averageEarnings)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                }
            }
        }
    }
}
