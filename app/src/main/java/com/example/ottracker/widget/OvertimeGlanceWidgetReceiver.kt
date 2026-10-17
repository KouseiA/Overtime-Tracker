package com.example.ottracker.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver that bridges Android's AppWidgetManager to the Jetpack Glance widget.
 * Registered in AndroidManifest.xml with overtime_glance_widget_info metadata.
 */
class OvertimeGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OvertimeGlanceWidget()
}
