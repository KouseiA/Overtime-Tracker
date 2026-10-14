package com.example.ottracker.ui.onboarding

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth

import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ottracker.theme.GoldAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color,
    val featureHighlights: List<String> = emptyList()
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.AccessTime,
        title = "Welcome to\nOT Tracker",
        subtitle = "Built by a worker, for workers.",
        description = "Tired of mentally calculating your overtime pay at the end of every shift? " +
                "OT Tracker was born from that exact frustration — a simple tool to make sure " +
                "every hour you work gets counted and every peso you earn is accounted for.",
        accentColor = GoldAccent
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.Assignment,
        title = "Log Your\nOvertime",
        subtitle = "OT Form — Your shift logbook",
        description = "Pick the date, set your start and end time, and tap save. " +
                "Your OT pay is calculated instantly — no more mental math after a long shift.",
        accentColor = Color(0xFF4FC3F7),
        featureHighlights = listOf(
            "⏰  Auto-calculates hours & pay",
            "🏷️  Holiday OT rate (+30% bonus)"
        )
    ),
    OnboardingPage(
        icon = Icons.Default.CalendarMonth,
        title = "Track Your\nHistory",
        subtitle = "Calendar — See it all at a glance",
        description = "Every shift you log appears as a gold dot on the calendar. " +
                "Tap any day to view, edit, or add shifts. " +
                "Your monthly total is always visible at the top.",
        accentColor = Color(0xFF81C784),
        featureHighlights = listOf(
            "📅  Visual calendar with OT markers",
            "✏️  Tap any day to edit shifts",
            "📊  Monthly earnings summary"
        )
    ),
    OnboardingPage(
        icon = Icons.Default.Settings,
        title = "Customize &\nProtect",
        subtitle = "Settings — Make it yours",
        description = "Set your hourly rate and default shift times, " +
                "and back up your data so you'll never lose a single record.",
        accentColor = Color(0xFFBA68C8),
        featureHighlights = listOf(
            "💰  Custom hourly rate & currency",
            "💾  Backup & restore your data"
        )
    ),
    OnboardingPage(
        icon = Icons.Default.Rocket,
        title = "You're All Set!",
        subtitle = "Start tracking your overtime today.",
        description = "Every hour counts. Every peso matters.\n\nLog your first OT shift now and take control of your earnings.",
        accentColor = GoldAccent,
        featureHighlights = listOf(
            "⚠️  Paalala: Ang halaga dito ay gabay at estimate lamang para sa sarili mong monitoring. Maaaring magkaiba ito sa official payslip depende sa company policy."
        )
    )
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(
                page = pages[pageIndex],
                pageIndex = pageIndex,
                isActive = pagerState.currentPage == pageIndex
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "indicator_width"
                    )
                    val color = if (isSelected) pages[pagerState.currentPage].accentColor
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Buttons
            if (isLastPage) {
                // "Get Started" button
                Button(
                    onClick = onFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Next + Skip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinished) {
                        Text(
                            "Skip",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[pagerState.currentPage].accentColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Next", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    isActive: Boolean
) {
    // Animate in when page becomes active
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isActive) {
        if (isActive) {
            visible = false
            delay(100)
            visible = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 80.dp, bottom = 160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Animated icon with orbit rings
        AnimatedIconWithOrbit(
            icon = page.icon,
            accentColor = page.accentColor,
            isActive = isActive
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Title
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        ) {
            Text(
                text = page.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500, delayMillis = 150)) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = tween(500, delayMillis = 150, easing = FastOutSlowInEasing)
            )
        ) {
            Text(
                text = page.subtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = page.accentColor,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(
                initialOffsetY = { 20 },
                animationSpec = tween(500, delayMillis = 300, easing = FastOutSlowInEasing)
            )
        ) {
            Text(
                text = page.description,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 23.sp
            )
        }

        // Feature highlights
        if (page.featureHighlights.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            page.featureHighlights.forEachIndexed { index, highlight ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 450 + index * 120)) + slideInVertically(
                        initialOffsetY = { 20 },
                        animationSpec = tween(
                            400,
                            delayMillis = 450 + index * 120,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                page.accentColor.copy(alpha = 0.08f)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = highlight,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedIconWithOrbit(
    icon: ImageVector,
    accentColor: Color,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbit")

    // Orbit rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation"
    )

    // Pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Scale-in when active
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(160.dp)
            .scale(scale)
    ) {
        // Orbital rings
        Canvas(
            modifier = Modifier.size(160.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 8f

            // Outer ring
            drawCircle(
                color = accentColor.copy(alpha = 0.12f),
                radius = radius,
                center = center,
                style = Stroke(width = 2f)
            )

            // Inner ring
            drawCircle(
                color = accentColor.copy(alpha = 0.08f),
                radius = radius * 0.7f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Orbiting dot
            val angleRad = Math.toRadians(rotation.toDouble())
            val dotX = center.x + radius * cos(angleRad).toFloat()
            val dotY = center.y + radius * sin(angleRad).toFloat()
            drawCircle(
                color = accentColor,
                radius = 5f,
                center = Offset(dotX, dotY)
            )

            // Second orbiting dot (opposite, smaller orbit)
            val angle2Rad = Math.toRadians((rotation + 180.0) % 360.0)
            val dot2X = center.x + radius * 0.7f * cos(angle2Rad).toFloat()
            val dot2Y = center.y + radius * 0.7f * sin(angle2Rad).toFloat()
            drawCircle(
                color = accentColor.copy(alpha = 0.6f),
                radius = 3.5f,
                center = Offset(dot2X, dot2Y)
            )
        }

        // Glow circle behind icon
        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.2f),
                            accentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = accentColor
        )
    }
}
