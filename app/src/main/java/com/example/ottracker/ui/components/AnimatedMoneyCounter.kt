package com.example.ottracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale

/**
 * "Cha-Ching!" Rolling Ticker Number Counter.
 * Smoothly animates from 0.00 (or previous amount) to the target amount like a digital
 * slot machine or bank ticker, complete with subtle haptic ticks and a final celebratory pop.
 */
@Composable
fun AnimatedMoneyCounter(
    targetAmount: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.displaySmall,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Black,
    durationMillis: Int = 1100,
    enableHaptics: Boolean = true,
    enableSound: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val animatableValue = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }
    var displayedValue by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(targetAmount) {
        if (enableSound) {
            MoneySound.init(context)
        }
        if (targetAmount <= 0.0) {
            animatableValue.snapTo(0f)
            displayedValue = 0.0
            return@LaunchedEffect
        }

        val end = targetAmount.toFloat()
        var lastTickTime = 0L

        animatableValue.animateTo(
            targetValue = end,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        ) {
            displayedValue = this.value.toDouble()

            // Hardware tick and sound every ~95ms during roll
            val now = System.currentTimeMillis()
            if (now - lastTickTime > 95) {
                lastTickTime = now
                if (enableHaptics) {
                    VibrationHelper.tick(context)
                }
                if (enableSound) {
                    MoneySound.playTick(context)
                }
            }
        }

        // Final celebratory pop and cha-ching when target amount is reached
        if (enableHaptics) {
            VibrationHelper.pop(context)
        }
        if (enableSound) {
            MoneySound.playChaChing(context)
        }

        // Final celebratory bounce when target amount is locked in
        try {
            scaleAnim.animateTo(
                targetValue = 1.05f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } catch (_: Exception) {}
    }

    Text(
        text = "$currencySymbol${String.format(Locale.US, "%,.2f", displayedValue)}",
        style = textStyle,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier.graphicsLayer {
            scaleX = scaleAnim.value
            scaleY = scaleAnim.value
        }
    )
}
