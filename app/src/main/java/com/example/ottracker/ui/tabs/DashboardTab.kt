package com.example.ottracker.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.OvertimeRecord
import com.example.ottracker.theme.GoldAccent
import com.example.ottracker.ui.components.AnimatedMoneyCounter
import com.example.ottracker.ui.components.EarningsBarChart
import com.example.ottracker.ui.components.GrindLevelBadge
import com.example.ottracker.ui.components.NeoCard
import com.example.ottracker.ui.components.NeoReceiptCard
import com.example.ottracker.ui.components.NeoStampBadge
import com.example.ottracker.ui.main.DashboardStats
import java.util.Locale

@Composable
fun DashboardTab(
    stats: DashboardStats,
    records: List<OvertimeRecord>,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        if (records.isEmpty()) {
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    borderColor = onSurface,
                    containerColor = surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO OVERTIME LOGGED YET",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Welcome to OT Tracker! Head over to the Calendar tab and tap any day to record your first overtime shift.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── 1. CURRENT CUTOFF HERO RECEIPT VOUCHER (P3 & P4) ───────────────────
        item {
            NeoReceiptCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 4.dp,
                stubContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "RENDERED HOURS",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.2f", stats.currentCutoff.hours)} HRS",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CYCLE STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .border(1.dp, GoldAccent, RoundedCornerShape(4.dp))
                                    .background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${stats.currentCutoff.daysRemaining}D REMAINING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = GoldAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "DOLE COMPLIANT • BASE RATE: ${settings.currencySymbol}${String.format(Locale.US, "%.2f", settings.baseHourlyRate)}/HR",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            ) {
                val rawPayday = stats.currentCutoff.payday
                val cleanPaydayDate = rawPayday.replace(Regex("(?i)^(payday:\\s*)+"), "").trim()

                // Header Row 1: Badges (Cycle Receipt + Grind Level)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoStampBadge(
                        text = "CYCLE RECEIPT",
                        rotation = -1.5f,
                        containerColor = GoldAccent.copy(alpha = 0.12f),
                        contentColor = GoldAccent,
                        borderColor = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    GrindLevelBadge(hours = stats.currentCutoff.hours)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Header Row 2: Cutoff Dates & Clean Payday (Zero overlap, perfectly spaced)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stats.currentCutoff.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cleanPaydayDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAYDAY: ${cleanPaydayDate.uppercase(Locale.US)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ESTIMATED OVERTIME PAYOUT",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp,
                    color = onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                AnimatedMoneyCounter(
                    targetAmount = stats.currentCutoff.pay,
                    currencySymbol = settings.currencySymbol,
                    textStyle = MaterialTheme.typography.displaySmall,
                    color = GoldAccent
                )
            }
        }

        // ── 2. PERIOD ACTIVITY ASYMMETRIC BENTO GRID (P4) ─────────────────────
        item {
            Text(
                text = "PERIOD BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Bento Tile: TODAY'S ACTIVITY (Primary Focal Tile)
                NeoCard(
                    modifier = Modifier.weight(1.15f),
                    shadowOffset = 3.dp,
                    shape = RoundedCornerShape(14.dp),
                    borderColor = onSurface,
                    containerColor = surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = onSurfaceVariant
                            )
                            // Live status dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (stats.todayHours > 0) Color(0xFF22C55E) else outline,
                                        shape = CircleShape
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${String.format(Locale.US, "%.1f", stats.todayHours)}h",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", stats.todayPay)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (stats.todayHours > 0) "ACTIVE SHIFT" else "OFF-DUTY",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (stats.todayHours > 0) Color(0xFF22C55E) else onSurfaceVariant
                        )
                    }
                }

                // Right Bento Column: THIS WEEK & THIS MONTH (Stacked Metrics)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // THIS WEEK Bento Tile
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowOffset = 3.dp,
                        shape = RoundedCornerShape(12.dp),
                        borderColor = onSurface,
                        containerColor = surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "THIS WEEK",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", stats.weekHours)}h",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = onSurface
                                )
                                Text(
                                    text = "${settings.currencySymbol}${String.format(Locale.US, "%.0f", stats.weekPay)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                            }
                        }
                    }

                    // THIS MONTH Bento Tile
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowOffset = 3.dp,
                        shape = RoundedCornerShape(12.dp),
                        borderColor = onSurface,
                        containerColor = surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "THIS MONTH",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", stats.monthHours)}h",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = onSurface
                                )
                                Text(
                                    text = "${settings.currencySymbol}${String.format(Locale.US, "%.0f", stats.monthPay)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 3. EARNINGS TRENDS BAR CHART BENTO ────────────────────────────────
        item {
            EarningsBarChart(
                cutoffTrends = stats.cutoffTrends,
                monthlyTrends = stats.monthlyTrends,
                currencySymbol = settings.currencySymbol
            )
        }

        // ── 4. HISTORICAL BENCHMARKS BENTO GRID ───────────────────────────────
        item {
            Text(
                text = "HISTORICAL BENCHMARKS",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Previous Cutoff Bento Tile
                NeoCard(
                    modifier = Modifier.weight(1f),
                    shadowOffset = 3.dp,
                    shape = RoundedCornerShape(14.dp),
                    borderColor = onSurface,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PREVIOUS CYCLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", stats.previousCutoff.pay)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", stats.previousCutoff.hours)}h rendered",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stats.previousCutoff.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // All-Time Career Bento Tile
                NeoCard(
                    modifier = Modifier.weight(1f),
                    shadowOffset = 3.dp,
                    shape = RoundedCornerShape(14.dp),
                    borderColor = onSurface,
                    containerColor = surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ALL-TIME",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = onSurfaceVariant
                            )
                            Text(
                                text = "${records.size} SHIFTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedMoneyCounter(
                            targetAmount = stats.totalPay,
                            currencySymbol = settings.currencySymbol,
                            textStyle = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            enableSound = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", stats.totalHours)}h logged",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Career Total Pay",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
