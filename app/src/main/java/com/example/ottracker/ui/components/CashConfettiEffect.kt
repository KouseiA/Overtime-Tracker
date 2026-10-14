package com.example.ottracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val size: Float,
    val type: ParticleType,
    val color: Color
)

private enum class ParticleType {
    COIN,
    CASH,
    SPARKLE
}

/**
 * Lightweight 25-particle Canvas Cash & Gold Confetti Burst.
 * Zero external libraries, hardware accelerated, auto-fades in 1.2s.
 */
@Composable
fun CashConfettiEffect(
    trigger: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }

    // Generate 24 fun particles
    val particles = remember {
        val rand = Random(System.currentTimeMillis())
        List(24) {
            val angle = rand.nextDouble(Math.PI * 0.15, Math.PI * 0.85) // shoot upwards
            val speed = rand.nextFloat() * 650f + 400f
            val pType = when (rand.nextInt(3)) {
                0 -> ParticleType.COIN
                1 -> ParticleType.CASH
                else -> ParticleType.SPARKLE
            }
            val pColor = when (pType) {
                ParticleType.COIN -> Color(0xFFFFD700)
                ParticleType.CASH -> Color(0xFF4CAF50)
                ParticleType.SPARKLE -> Color(0xFFFFEB3B)
            }
            ConfettiParticle(
                initialX = 0.5f,
                initialY = 0.65f, // shoot up from lower center
                velocityX = (Math.cos(angle) * speed).toFloat() * if (rand.nextBoolean()) 1f else -1f,
                velocityY = -(Math.sin(angle) * speed).toFloat(),
                rotationSpeed = rand.nextFloat() * 360f - 180f,
                size = rand.nextFloat() * 12f + 14f,
                type = pType,
                color = pColor
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val canvasWidth = size.width
        val canvasHeight = size.height
        val gravity = 1200f * t * t // Accelerating gravity drop
        val alpha = (1f - t * 1.1f).coerceIn(0f, 1f)

        for (p in particles) {
            val currentX = (p.initialX * canvasWidth) + (p.velocityX * t)
            val currentY = (p.initialY * canvasHeight) + (p.velocityY * t) + gravity
            val currentRotation = p.rotationSpeed * t

            rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                when (p.type) {
                    ParticleType.COIN -> {
                        // Gold coin with dark border
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size / 2f,
                            center = Offset(currentX, currentY)
                        )
                        drawCircle(
                            color = Color.Black.copy(alpha = alpha),
                            radius = p.size / 2f,
                            center = Offset(currentX, currentY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                    ParticleType.CASH -> {
                        // Green Dollar Bill
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(currentX - p.size, currentY - p.size / 2),
                            size = Size(p.size * 2f, p.size)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = alpha),
                            topLeft = Offset(currentX - p.size, currentY - p.size / 2),
                            size = Size(p.size * 2f, p.size),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                    ParticleType.SPARKLE -> {
                        // Sparkle star
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size / 3f,
                            center = Offset(currentX, currentY)
                        )
                    }
                }
            }
        }
    }
}
