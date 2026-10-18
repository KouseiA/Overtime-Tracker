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
        trackColor: Int = Color.parseColor("#262626"),
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
        if (clampedProgress >= 0.999f) {
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokeWidthPx
                color = progressColor
            }
            canvas.drawOval(rect, progressPaint)
        } else if (clampedProgress > 0.005f) {
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

    /**
     * Draws a horizontal progress bar with dynamically aligned milestone labels.
     * The current hours text is positioned directly under the tip of the progress fill.
     */
    fun drawHorizontalProgressBar(
        widthPx: Int = 700,
        heightPx: Int = 68,
        progress: Float,
        currentHoursText: String,
        targetHoursText: String,
        trackColor: Int = Color.parseColor("#1E2633"),
        progressColor: Int = Color.parseColor("#FFD700"),
        textColorMuted: Int = Color.parseColor("#94A3B8")
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val barHeight = 24f
        val barRadius = 12f
        val clampedProgress = progress.coerceIn(0.0f, 1.0f)

        // 1. Draw track
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = trackColor
        }
        val trackRect = RectF(0f, 0f, widthPx.toFloat(), barHeight)
        canvas.drawRoundRect(trackRect, barRadius, barRadius, trackPaint)

        // 2. Draw active progress fill
        if (clampedProgress > 0.005f) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = progressColor
            }
            val fillWidth = (widthPx * clampedProgress).coerceAtLeast(barRadius * 2f)
            val fillRect = RectF(0f, 0f, fillWidth, barHeight)
            canvas.drawRoundRect(fillRect, barRadius, barRadius, fillPaint)
        }

        // 3. Draw Milestones
        val textY = 56f
        val milestonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 21f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // 3a. Start label "0"
        milestonePaint.color = textColorMuted
        milestonePaint.textAlign = Paint.Align.LEFT
        canvas.drawText("0", 0f, textY, milestonePaint)

        // 3b. Target label (e.g. "30+")
        milestonePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(targetHoursText, widthPx.toFloat(), textY, milestonePaint)

        // 3c. Current hours label (e.g. "4.5 hrs") - perfectly anchored to the tip
        milestonePaint.color = progressColor
        milestonePaint.textAlign = Paint.Align.CENTER
        val currentTextWidth = milestonePaint.measureText(currentHoursText)
        val tipX = widthPx * clampedProgress
        val minX = currentTextWidth / 2f + 32f
        val maxX = widthPx.toFloat() - currentTextWidth / 2f - 48f
        val clampedTipX = tipX.coerceIn(minX, maxX)
        canvas.drawText(currentHoursText, clampedTipX, textY, milestonePaint)

        return bitmap
    }
}
