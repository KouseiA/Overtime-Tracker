package com.example.ottracker.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Direct hardware vibration motor helper.
 * Bypasses OS touch vibration drops so the phone physically vibrates reliably
 * on all Android brands (Xiaomi, Samsung, Realme, Oppo, Vivo, Motorola, Pixel).
 */
object VibrationHelper {

    /**
     * Crisp mechanical arcade click (30ms).
     */
    fun click(context: Context) {
        vibrate(context, durationMs = 30, amplitude = 200)
    }

    /**
     * Satisfying punchy cash pop (60ms).
     */
    fun pop(context: Context) {
        vibrate(context, durationMs = 60, amplitude = 255)
    }

    /**
     * Rapid slot-machine ticker buzz (15ms).
     */
    fun tick(context: Context) {
        vibrate(context, durationMs = 15, amplitude = 120)
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = amplitude.coerceIn(1, 255)
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
