package com.example.ottracker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.ottracker.ui.main.MainScreen
import com.example.ottracker.ui.onboarding.OnboardingScreen

@Composable
fun MainNavigation(
    targetTab: String? = null,
    showOnboarding: Boolean = false,
    onOnboardingComplete: () -> Unit = {}
) {
    Crossfade(
        targetState = showOnboarding,
        animationSpec = tween(400),
        label = "onboarding_transition"
    ) { shouldShowOnboarding ->
        if (shouldShowOnboarding) {
            OnboardingScreen(
                onFinished = onOnboardingComplete
            )
        } else {
            val backStack = rememberNavBackStack(Main)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider =
                    entryProvider {
                        entry<Main> {
                            MainScreen(
                                targetTab = targetTab,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
            )
        }
    }
}
