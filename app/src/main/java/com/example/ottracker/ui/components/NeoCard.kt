package com.example.ottracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Neo-Brutalist Card with a solid hard drop shadow.
 * When [onClick] is provided, pressing the card physically sinks it down-right by [shadowOffset],
 * squishing the shadow like a tactile physical arcade button.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    shadowOffset: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderWidth: Dp = 1.5.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedOffset by animateDpAsState(
        targetValue = if (onClick != null && isPressed) shadowOffset else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "neo_press_card"
    )

    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Hard drop shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )

        // Foreground card with physical press translation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffset, y = animatedOffset)
                .border(borderWidth, borderColor, shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null, // Custom 3D arcade press provides the visual feedback
                            onClick = {
                                VibrationHelper.click(context)
                                onClick()
                            }
                        )
                    } else Modifier
                ),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    }
}

/**
 * Tactile 3D Neo-Brutalist Arcade Button.
 * Physically depresses into its shadow when pressed down, with satisfying spring release and hardware click.
 */
@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shadowOffset: Dp = 3.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    containerColor: Color = MaterialTheme.colorScheme.onSurface,
    contentColor: Color = MaterialTheme.colorScheme.background,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    borderWidth: Dp = 1.5.dp,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedOffset by animateDpAsState(
        targetValue = if (enabled && isPressed) shadowOffset else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "neo_button_press"
    )

    val actualContainer = if (enabled) containerColor else containerColor.copy(alpha = 0.35f)
    val actualBorder = if (enabled) borderColor else borderColor.copy(alpha = 0.35f)
    val actualShadow = if (enabled) shadowColor else shadowColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset),
        propagateMinConstraints = true
    ) {
        // Shadow base
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(actualShadow, shape)
        )

        // Front button surface
        Box(
            modifier = Modifier
                .offset(x = animatedOffset, y = animatedOffset)
                .border(borderWidth, actualBorder, shape)
                .background(actualContainer, shape)
                .then(
                    if (enabled) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                VibrationHelper.click(context)
                                onClick()
                            }
                        )
                    } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

/**
 * Tactile 3D Neo-Brutalist Icon Button (e.g. Month Arrows, Close, Delete).
 */
@Composable
fun NeoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    shadowOffset: Dp = 2.5.dp,
    shape: Shape = RoundedCornerShape(10.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    borderWidth: Dp = 1.5.dp,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "neo_icon_press"
    )

    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )

        Box(
            modifier = Modifier
                .size(size)
                .offset(x = animatedOffset, y = animatedOffset)
                .border(borderWidth, borderColor, shape)
                .background(containerColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        VibrationHelper.click(context)
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
