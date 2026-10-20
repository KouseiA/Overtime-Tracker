package com.example.ottracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ottracker.data.OvertimeType
import com.example.ottracker.theme.GoldAccent

/**
 * An authentic Neo-Brutalist "Rubber Stamp" / industrial inspection badge.
 * Features a thick outline, high-contrast monospace typography, and an optional subtle tilt.
 */
@Composable
fun NeoStampBadge(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    rotation: Float = 0f,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    borderWidth: Dp = 1.5.dp,
    fontSize: TextUnit = 10.sp,
    cornerRadius: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .rotate(rotation)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
            .background(containerColor, RoundedCornerShape(cornerRadius))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (emoji != null) {
                Text(
                    text = emoji,
                    fontSize = (fontSize.value + 1).sp
                )
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = 0.8.sp,
                color = contentColor
            )
        }
    }
}

/**
 * Domain-specific stamped badges for DOLE statutory overtime rate categories.
 */
@Composable
fun OvertimeCategoryStamp(
    overtimeType: String,
    modifier: Modifier = Modifier,
    rotation: Float = 0f
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        when (overtimeType) {
            OvertimeType.REGULAR_HOLIDAY -> {
                // Gold Double Pay Stamp
                NeoStampBadge(
                    text = "RH 2.0×",
                    emoji = "🎉",
                    rotation = rotation,
                    containerColor = GoldAccent,
                    contentColor = Color.Black,
                    borderColor = onSurface,
                    borderWidth = 1.5.dp
                )
            }
            OvertimeType.SPECIAL_HOLIDAY -> {
                // Special Holiday +30% Stamp
                NeoStampBadge(
                    text = "SNWH +30%",
                    emoji = "🏖️",
                    rotation = rotation,
                    containerColor = GoldAccent.copy(alpha = 0.22f),
                    contentColor = onSurface,
                    borderColor = onSurface,
                    borderWidth = 1.5.dp
                )
            }
            else -> {
                // Regular OT Stamp
                NeoStampBadge(
                    text = "REG OT",
                    emoji = "⚡",
                    rotation = rotation,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = onSurface,
                    borderColor = onSurface,
                    borderWidth = 1.5.dp
                )
            }
        }
    }
}
