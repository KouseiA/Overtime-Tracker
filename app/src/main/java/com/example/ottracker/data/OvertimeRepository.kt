package com.example.ottracker.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.example.ottracker.widget.CompactGaugeWidgetProvider
import com.example.ottracker.widget.OvertimeGlanceWidget
import com.example.ottracker.widget.OvertimeGlanceWidgetReceiver
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OvertimeRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("ot_tracker_prefs", Context.MODE_PRIVATE)
    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _records = MutableStateFlow<List<OvertimeRecord>>(emptyList())
    val records: StateFlow<List<OvertimeRecord>> = _records.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
        loadRecords()
    }

    private fun loadSettings() {
        val jsonStr = prefs.getString(KEY_SETTINGS, null)
        if (jsonStr != null) {
            try {
                _settings.value = AppSettings.fromJsonObject(JSONObject(jsonStr))
            } catch (_: Exception) {}
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit().putString(KEY_SETTINGS, newSettings.toJsonObject().toString()).commit()
        updateAllWidgets()
    }

    private fun loadRecords() {
        val jsonStr = prefs.getString(KEY_RECORDS, null)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = ArrayList<OvertimeRecord>()
                for (i in 0 until array.length()) {
                    list.add(OvertimeRecord.fromJsonObject(array.getJSONObject(i)))
                }
                _records.value = list.sortedByDescending { it.startTimeMillis }
                return
            } catch (_: Exception) {}
        }
        _records.value = emptyList()
    }

    fun addRecord(record: OvertimeRecord) {
        val current = _records.value.toMutableList()
        // Replace if already exists for same id, or insert
        val existingIndex = current.indexOfFirst { it.id == record.id }
        if (existingIndex >= 0) {
            current[existingIndex] = record
        } else {
            current.add(0, record)
        }
        saveRecords(current.sortedByDescending { it.startTimeMillis })
    }

    fun deleteRecord(id: String) {
        val filtered = _records.value.filter { it.id != id }
        saveRecords(filtered)
    }

    fun clearAllRecords() {
        saveRecords(emptyList())
    }

    private fun saveRecords(list: List<OvertimeRecord>) {
        _records.value = list
        val array = JSONArray()
        list.forEach { array.put(it.toJsonObject()) }
        prefs.edit().putString(KEY_RECORDS, array.toString()).commit()
        updateAllWidgets()
    }

    private fun updateAllWidgets() {
        // 1. Immediately update RemoteViews widgets (Compact Circular Gauge)
        CompactGaugeWidgetProvider.updateAllWidgets(appContext)

        // 2. Direct Android OS broadcast to wake glance receiver immediately
        try {
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            if (appWidgetManager != null) {
                val glanceComponent = ComponentName(appContext, OvertimeGlanceWidgetReceiver::class.java)
                val glanceIds = appWidgetManager.getAppWidgetIds(glanceComponent)
                if (glanceIds != null && glanceIds.isNotEmpty()) {
                    val updateIntent = Intent(appContext, OvertimeGlanceWidgetReceiver::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, glanceIds)
                    }
                    appContext.sendBroadcast(updateIntent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast update to Glance receiver", e)
        }

        // 3. Update Jetpack Glance widgets with state invalidation to bypass session throttling
        widgetScope.launch {
            try {
                val glanceManager = GlanceAppWidgetManager(appContext)
                val glanceIds = glanceManager.getGlanceIds(OvertimeGlanceWidget::class.java)
                val timestamp = System.currentTimeMillis()

                for (glanceId in glanceIds) {
                    try {
                        updateAppWidgetState(appContext, glanceId) { prefs ->
                            prefs[OvertimeGlanceWidget.KEY_UPDATE_TIME] = timestamp
                        }
                        OvertimeGlanceWidget().update(appContext, glanceId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update Glance widget instance: $glanceId", e)
                    }
                }
                // Fallback catch-all
                OvertimeGlanceWidget().updateAll(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Failed updating Glance widgets in scope", e)
            }
        }
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("settings", _settings.value.toJsonObject())
        val array = JSONArray()
        _records.value.forEach { array.put(it.toJsonObject()) }
        root.put("records", array)
        return root.toString(2)
    }

    fun importBackupJson(jsonStr: String, replaceExisting: Boolean = false): Int {
        val trimmed = jsonStr.trim()
        var importedCount = 0
        if (trimmed.startsWith("{")) {
            val root = JSONObject(trimmed)
            if (root.has("settings")) {
                try {
                    val newSettings = AppSettings.fromJsonObject(root.getJSONObject("settings"))
                    updateSettings(newSettings)
                } catch (_: Exception) {}
            }
            if (root.has("records")) {
                val array = root.getJSONArray("records")
                importedCount = importRecordsFromArray(array, replaceExisting)
            }
        } else if (trimmed.startsWith("[")) {
            val array = JSONArray(trimmed)
            importedCount = importRecordsFromArray(array, replaceExisting)
        } else {
            throw IllegalArgumentException("Invalid backup format: Must be a valid JSON backup.")
        }
        return importedCount
    }

    private fun importRecordsFromArray(array: JSONArray, replaceExisting: Boolean): Int {
        val targetList = if (replaceExisting) mutableListOf() else _records.value.toMutableList()
        var count = 0
        for (i in 0 until array.length()) {
            val rec = OvertimeRecord.fromJsonObject(array.getJSONObject(i))
            val existingIdx = targetList.indexOfFirst { it.id == rec.id }
            if (existingIdx >= 0) {
                targetList[existingIdx] = rec
            } else {
                targetList.add(rec)
            }
            count++
        }
        saveRecords(targetList.sortedByDescending { it.startTimeMillis })
        return count
    }

    val hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    companion object {
        private const val TAG = "OvertimeRepository"
        private const val KEY_RECORDS = "records_json"
        private const val KEY_SETTINGS = "settings_json"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
