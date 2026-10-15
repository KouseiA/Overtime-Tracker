package com.example.ottracker.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object GaugeDrawer {

    /**
     * Draws a circular progress ring gauge on a Bitmap.
     *
     * @param sizePx Diameter of the bitmap (e.g. 260px)
     * @param strokeWidthPx Thickness of the ring stroke
     * @param progress Percentage 0.0f to 1.0f (or higher)
     * @param trackColor Color for the background full circle (unfilled)
     * @param progressColor Color for the active progress arc (e.g. Gold #FFD700)
     */
    fun drawCircularGauge(
        sizePx: Int = 260,
        strokeWidthPx: Float = 22f,
        progress: Float,
        trackColor: Int = Color.parseColor("#1E293B"),
        progressColor: Int = Color.parseColor("#FFD700")
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val halfStroke = strokeWidthPx / 2f
        val rect = RectF(
            halfStroke + 4f,
            halfStroke + 4f,
            sizePx.toFloat() - halfStroke - 4f,
            sizePx.toFloat() - halfStroke - 4f
        )

        // 1. Draw track background circle
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            color = trackColor
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawOval(rect, trackPaint)

        // 2. Draw active progress arc (starting at -90 degrees, which is 12 o'clock)
        val clampedProgress = progress.coerceIn(0.0f, 1.0f)
        if (clampedProgress > 0.005f) {
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokeWidthPx
                color = progressColor
                strokeCap = Paint.Cap.ROUND
            }
            val sweepAngle = clampedProgress * 360f
            canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)
        }

        return bitmap
    }
}
