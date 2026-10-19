package com.example.ottracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A tactile Neo-Brutalist "Payroll Receipt / Punch-Card Voucher" Card.
 * Features hard borders, solid drop shadow, and a physical perforated tear-off line
 * with semicircular ticket notches on both edges.
 */
@Composable
fun NeoReceiptCard(
    modifier: Modifier = Modifier,
    shadowOffset: Dp = 4.dp,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderWidth: Dp = 1.5.dp,
    stubContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(16.dp)

    Box(modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        // 1. Solid Hard Drop Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(borderColor, shape)
        )

        // 2. Physical Card Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(borderWidth, borderColor, shape)
                .background(containerColor, shape)
        ) {
            // Main Top Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                content = content
            )

            // Optional Stub with Perforated Tear Line
            if (stubContent != null) {
                PerforatedTearDivider(
                    cardBackground = containerColor,
                    borderColor = borderColor,
                    borderWidth = borderWidth
                )

                // Bottom Voucher Stub
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    content = stubContent
                )
            }
        }
    }
}

/**
 * Perforated dash line with left & right semicircular ticket punch notches.
 */
@Composable
fun PerforatedTearDivider(
    cardBackground: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.5.dp,
    notchRadius: Dp = 9.dp
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(notchRadius * 2),
        contentAlignment = Alignment.Center
    ) {
        // Dashed Tear-Off Line across the center
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = notchRadius + 4.dp)
                .height(1.dp)
        ) {
            drawLine(
                color = onSurfaceVariant.copy(alpha = 0.5f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
        }

        // Left Notch (Punch Hole)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = -notchRadius)
                .size(notchRadius * 2)
                .background(MaterialTheme.colorScheme.background, CircleShape)
                .border(borderWidth, borderColor, CircleShape)
        )

        // Right Notch (Punch Hole)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = notchRadius)
                .size(notchRadius * 2)
                .background(MaterialTheme.colorScheme.background, CircleShape)
                .border(borderWidth, borderColor, CircleShape)
        )
    }
}
