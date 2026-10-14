package com.example.ottracker.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ottracker.data.OvertimeRecord
import com.example.ottracker.data.OvertimeRepository
import com.example.ottracker.theme.GoldAccent
import com.example.ottracker.ui.components.CashConfettiEffect
import com.example.ottracker.ui.tabs.CalendarTab
import com.example.ottracker.ui.tabs.DashboardTab
import com.example.ottracker.ui.tabs.OtFormTab
import com.example.ottracker.ui.tabs.SettingsTab
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Statistics", Icons.Default.Assessment),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    OT_FORM("OT Form", Icons.AutoMirrored.Filled.Assignment),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    targetTab: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val repository = remember { OvertimeRepository(context) }
    val viewModel: MainScreenViewModel = viewModel(factory = MainScreenViewModel.Factory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTabIndex by remember {
        val initial = when (targetTab) {
            "CALENDAR" -> AppTab.CALENDAR.ordinal
            "OT_FORM" -> AppTab.OT_FORM.ordinal
            else -> 0
        }
        mutableIntStateOf(initial)
    }

    androidx.compose.runtime.LaunchedEffect(targetTab) {
        when (targetTab) {
            "CALENDAR" -> selectedTabIndex = AppTab.CALENDAR.ordinal
            "OT_FORM" -> selectedTabIndex = AppTab.OT_FORM.ordinal
        }
    }

    var showConfetti by remember { mutableStateOf(false) }

    val handleAddOrUpdateRecord: (OvertimeRecord) -> Unit = { record ->
        com.example.ottracker.ui.components.VibrationHelper.pop(context)
        showConfetti = true
        viewModel.addOrUpdateRecord(record)
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val hours = record.durationHours
            val formattedHours = String.format(Locale.US, "%.1f", hours)
            val formattedPay = String.format(Locale.US, "%.2f", record.totalPay)
            snackbarHostState.showSnackbar(
                message = "OT shift saved: ${formattedHours}h (${state.settings.currencySymbol}$formattedPay)",
                duration = SnackbarDuration.Short
            )
        }
    }

    val handleDeleteRecord: (String) -> Unit = { id ->
        val recordToDelete = state.records.find { it.id == id }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.deleteRecord(id)
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "OT shift deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && recordToDelete != null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.addOrUpdateRecord(recordToDelete)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    actionColor = GoldAccent
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (AppTab.entries[selectedTabIndex]) {
                            AppTab.DASHBOARD -> "OT Statistics"
                            AppTab.CALENDAR -> "Overtime Calendar"
                            AppTab.OT_FORM -> "OT Form Records Sheet"
                            AppTab.SETTINGS -> "Settings"
                        },
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = {
                            if (selectedTabIndex != index) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedTabIndex = index
                            }
                        },
                        icon = {
                            Icon(tab.icon, contentDescription = tab.title)
                        },
                        label = {
                            Text(
                                tab.title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = AppTab.entries[selectedTabIndex],
                animationSpec = tween(200),
                label = "tab_crossfade"
            ) { currentTab ->
                when (currentTab) {
                    AppTab.DASHBOARD -> DashboardTab(
                        stats = state.stats,
                        records = state.records,
                        settings = state.settings
                    )

                    AppTab.CALENDAR -> CalendarTab(
                        records = state.records,
                        settings = state.settings,
                        onAddOrUpdateRecord = handleAddOrUpdateRecord,
                        onDeleteRecord = handleDeleteRecord
                    )

                    AppTab.OT_FORM -> OtFormTab(
                        records = state.records,
                        stats = state.stats,
                        settings = state.settings,
                        onAddOrUpdateRecord = handleAddOrUpdateRecord,
                        onDeleteRecord = handleDeleteRecord
                    )

                    AppTab.SETTINGS -> SettingsTab(
                        settings = state.settings,
                        recordCount = state.records.size,
                        onSaveSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                        onClearAllData = { viewModel.clearAllRecords() },
                        onExportBackup = { viewModel.exportBackupJson() },
                        onImportBackup = { json, replace -> viewModel.importBackupJson(json, replace) }
                    )
                }
            }

            CashConfettiEffect(
                trigger = showConfetti,
                onFinished = { showConfetti = false }
            )
        }
    }
}

