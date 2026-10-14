package com.example.ottracker.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ottracker.data.AppSettings
import com.example.ottracker.data.OvertimeRecord
import com.example.ottracker.theme.GoldAccent
import com.example.ottracker.ui.components.AnimatedMoneyCounter
import com.example.ottracker.ui.components.EarningsBarChart
import com.example.ottracker.ui.components.GrindLevelBadge
import com.example.ottracker.ui.components.NeoCard
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

        // ── 1. CURRENT CUTOFF HERO CARD ──────────────────────────────────────
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(16.dp),
                borderColor = onSurface,
                containerColor = surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Header row with Grind Level Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CURRENT CUTOFF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            GrindLevelBadge(hours = stats.currentCutoff.hours)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stats.currentCutoff.payday,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stats.currentCutoff.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Estimated Overtime Payout",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    AnimatedMoneyCounter(
                        targetAmount = stats.currentCutoff.pay,
                        currencySymbol = settings.currencySymbol,
                        textStyle = MaterialTheme.typography.displaySmall,
                        color = GoldAccent
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = outline)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.2f", stats.currentCutoff.hours)} Hours Rendered",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurface
                        )
                        Text(
                            text = "${stats.currentCutoff.daysRemaining}d left",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── 2. PERIOD OVERVIEW LABEL ────────────────────────────────────────
        item {
            Text(
                text = "PERIOD OVERVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = onSurfaceVariant
            )
        }

        // ── 3. THREE PERIOD CARDS: TODAY / THIS WEEK / THIS MONTH ───────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MonoPeriodCard(
                    title = "TODAY",
                    hours = stats.todayHours,
                    pay = stats.todayPay,
                    currencySymbol = settings.currencySymbol,
                    modifier = Modifier.weight(1f)
                )
                MonoPeriodCard(
                    title = "THIS WEEK",
                    hours = stats.weekHours,
                    pay = stats.weekPay,
                    currencySymbol = settings.currencySymbol,
                    modifier = Modifier.weight(1f)
                )
                MonoPeriodCard(
                    title = "THIS MONTH",
                    hours = stats.monthHours,
                    pay = stats.monthPay,
                    currencySymbol = settings.currencySymbol,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── 3.5. EARNINGS TRENDS BAR CHART ──────────────────────────────────
        item {
            EarningsBarChart(
                cutoffTrends = stats.cutoffTrends,
                monthlyTrends = stats.monthlyTrends,
                currencySymbol = settings.currencySymbol
            )
        }

        // ── 4. PREVIOUS CUTOFF CARD ─────────────────────────────────────────
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(16.dp),
                borderColor = onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREVIOUS CUTOFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = onSurfaceVariant
                        )
                        Text(
                            text = stats.previousCutoff.payday,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stats.previousCutoff.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.2f", stats.previousCutoff.hours)} hrs rendered",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurface
                            )
                        }

                        Text(
                            text = "${settings.currencySymbol}${String.format(Locale.US, "%,.2f", stats.previousCutoff.pay)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                    }
                }
            }
        }

        // ── 5. ALL-TIME TOTAL CARD ───────────────────────────────────────────
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(16.dp),
                borderColor = onSurface,
                containerColor = surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ALL-TIME TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedMoneyCounter(
                            targetAmount = stats.totalPay,
                            currencySymbol = settings.currencySymbol,
                            textStyle = MaterialTheme.typography.titleLarge,
                            color = GoldAccent,
                            enableSound = false
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format(Locale.US, "%.2f", stats.totalHours)} hrs",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${records.size} OT Shifts Logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun MonoPeriodCard(
    title: String,
    hours: Double,
    pay: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface

    NeoCard(
        modifier = modifier,
        shadowOffset = 3.dp,
        shape = RoundedCornerShape(14.dp),
        borderColor = onSurface,
        containerColor = surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                fontSize = 10.sp,
                color = onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${String.format(Locale.US, "%.1f", hours)}h",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%.0f", pay)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
