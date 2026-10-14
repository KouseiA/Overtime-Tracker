package com.example.ottracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ottracker.data.OvertimeRepository
import com.example.ottracker.theme.OTTrackerTheme

class MainActivity : ComponentActivity() {
  private var targetTab by mutableStateOf<String?>(null)
  private var showOnboarding by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    targetTab = intent?.getStringExtra("OPEN_TAB")

    // Check if user has completed onboarding
    val repository = OvertimeRepository(this)
    showOnboarding = !repository.hasCompletedOnboarding

    enableEdgeToEdge()
    setContent {
      OTTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(
            targetTab = targetTab,
            showOnboarding = showOnboarding,
            onOnboardingComplete = {
              repository.setOnboardingCompleted()
              showOnboarding = false
            }
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.getStringExtra("OPEN_TAB")?.let {
      targetTab = it
    }
  }
}
