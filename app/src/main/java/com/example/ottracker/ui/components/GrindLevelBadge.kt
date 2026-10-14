package com.example.ottracker.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ottracker.theme.GoldAccent

data class GrindTier(
    val emoji: String,
    val title: String,
    val containerColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val shouldPulse: Boolean = false
)

fun getGrindTier(hours: Double): GrindTier {
    return when {
        hours >= 20.0 -> GrindTier(
            emoji = "👑",
            title = "PAYDAY KING",
            containerColor = GoldAccent,
            textColor = Color.Black,
            borderColor = Color.Black,
            shouldPulse = true
        )
        hours >= 10.0 -> GrindTier(
            emoji = "🔥",
            title = "FLAME ON!",
            containerColor = Color(0xFFFF5722),
            textColor = Color.White,
            borderColor = Color.Black,
            shouldPulse = true
        )
        hours >= 5.0 -> GrindTier(
            emoji = "⚡",
            title = "SPARK MODE",
            containerColor = Color(0xFFFFC107),
            textColor = Color.Black,
            borderColor = Color.Black,
            shouldPulse = false
        )
        else -> GrindTier(
            emoji = "☕",
            title = "WARM-UP",
            containerColor = Color(0xFF2C2C2E),
            textColor = Color(0xFFE5E5EA),
            borderColor = Color(0xFF48484A),
            shouldPulse = false
        )
    }
}

/**
 * Animated Grind Level Milestone Badge.
 * Displayed on the Hero Card to visually celebrate user overtime effort.
 */
@Composable
fun GrindLevelBadge(
    hours: Double,
    modifier: Modifier = Modifier
) {
    val tier = getGrindTier(hours)

    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by if (tier.shouldPulse) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "static_scale"
        )
    }

    Box(
        modifier = modifier
            .scale(pulseScale)
            .border(1.5.dp, tier.borderColor, RoundedCornerShape(8.dp))
            .background(tier.containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = tier.emoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = tier.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = tier.textColor
            )
        }
    }
}
