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

# Compose optimization: rely on library consumer rules and don't force-retain all icons
-dontwarn androidx.compose.**
-dontwarn androidx.compose.material.icons.**
