# ── OT Tracker ProGuard / R8 Rules ────────────────────────────────────────────

# Keep data classes used with JSONObject serialization (accessed via reflection-like patterns)
-keep class com.example.ottracker.data.OvertimeRecord { *; }
-keep class com.example.ottracker.data.AppSettings { *; }

# Keep the widget provider (referenced in AndroidManifest.xml)
-keep class com.example.ottracker.widget.OvertimeWidgetProvider { *; }

# Keep Compose — required for R8 full mode
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Material3 icons (extended icons are tree-shaken by R8 automatically,
# but we need to keep the ones actually referenced)
-dontwarn androidx.compose.material.icons.**
