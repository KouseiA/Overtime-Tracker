# ── OT Tracker ProGuard / R8 Rules ────────────────────────────────────────────

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep data classes used with JSON serialization
-keep class com.example.ottracker.data.** { *; }

# Keep all widget providers and helpers
-keep class com.example.ottracker.widget.** { *; }

# Jetpack Glance & WorkManager
-dontwarn androidx.glance.**
-keep class androidx.glance.** { *; }
-dontwarn androidx.work.**
-keep class androidx.work.** { *; }

# Keep Compose — required for R8 full mode
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Material3 icons
-dontwarn androidx.compose.material.icons.**
