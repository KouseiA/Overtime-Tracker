package com.example.ottracker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ottracker.widget.OvertimeWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class OvertimeRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("ot_tracker_prefs", Context.MODE_PRIVATE)

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
        prefs.edit().putString(KEY_SETTINGS, newSettings.toJsonObject().toString()).apply()
        OvertimeWidgetProvider.updateAllWidgets(appContext)
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
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply()
        OvertimeWidgetProvider.updateAllWidgets(appContext)
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
        private const val KEY_RECORDS = "records_json"
        private const val KEY_SETTINGS = "settings_json"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
