package com.example.ottracker.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates and plays money-counting sound effects programmatically via Android SoundPool.
 * No external asset/mp3 files needed — pure synthesized tones converted to tiny cached WAV files.
 *
 * - playTick(): Crisp, rapid bill/coin counter tick during number rolling (~30ms)
 * - playChaChing(): Rich metallic cash register "cha-ching" chime on finish (~320ms)
 */
object MoneySound {
    private const val SAMPLE_RATE = 22050
    private var soundPool: SoundPool? = null
    private var tickSoundId = 0
    private var chaChingSoundId = 0
    private var isLoaded = false
    private var isInitializing = false

    fun init(context: Context) {
        if (isLoaded || isInitializing) return
        isInitializing = true
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool = pool

            val cacheDir = context.applicationContext.cacheDir
            val tickFile = File(cacheDir, "money_tick.wav")
            val chaChingFile = File(cacheDir, "money_chaching.wav")

            if (!tickFile.exists()) {
                val tickWav = generateTickWav()
                FileOutputStream(tickFile).use { it.write(tickWav) }
            }
            if (!chaChingFile.exists()) {
                val chaChingWav = generateChaChingWav()
                FileOutputStream(chaChingFile).use { it.write(chaChingWav) }
            }

            var loadedCount = 0
            pool.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    loadedCount++
                    if (loadedCount >= 2) {
                        isLoaded = true
                    }
                }
            }

            tickSoundId = pool.load(tickFile.absolutePath, 1)
            chaChingSoundId = pool.load(chaChingFile.absolutePath, 1)
        } catch (_: Exception) {
            isInitializing = false
        }
    }

    fun playTick(context: Context) {
        init(context)
        if (isLoaded && tickSoundId != 0) {
            soundPool?.play(tickSoundId, 0.5f, 0.5f, 1, 0, 1.0f)
        }
    }

    fun playChaChing(context: Context) {
        init(context)
        if (isLoaded && chaChingSoundId != 0) {
            soundPool?.play(chaChingSoundId, 0.75f, 0.75f, 2, 0, 1.0f)
        }
    }

    private fun generateTickWav(): ByteArray {
        val durationMs = 30
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-t * 140.0)
            val wave = sin(2 * PI * 3400 * t) * 0.55 +
                    sin(2 * PI * 5800 * t) * 0.3 +
                    sin(2 * PI * 8200 * t) * 0.15
            samples[i] = (wave * envelope * 9000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return pcmToWav(samples, SAMPLE_RATE)
    }

    private fun generateChaChingWav(): ByteArray {
        val durationMs = 320
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val samples = ShortArray(numSamples)
        val chime1Samples = (SAMPLE_RATE * 0.12).toInt()

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val wave: Double
            if (i < chime1Samples) {
                // First bell chime (E6 ~1318 Hz)
                val env = exp(-t * 22.0)
                wave = (sin(2 * PI * 1318 * t) * 0.5 +
                        sin(2 * PI * 2636 * t) * 0.25 +
                        sin(2 * PI * 3954 * t) * 0.15 +
                        sin(2 * PI * 5274 * t) * 0.1) * env
            } else {
                // Second higher bell chime (A6 ~1760 Hz)
                val t2 = (i - chime1Samples).toDouble() / SAMPLE_RATE
                val env = exp(-t2 * 12.0)
                wave = (sin(2 * PI * 1760 * t2) * 0.5 +
                        sin(2 * PI * 3520 * t2) * 0.25 +
                        sin(2 * PI * 5280 * t2) * 0.15 +
                        sin(2 * PI * 7040 * t2) * 0.1) * env
            }
            samples[i] = (wave * 14000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return pcmToWav(samples, SAMPLE_RATE)
    }

    private fun pcmToWav(samples: ShortArray, sampleRate: Int): ByteArray {
        val dataSize = samples.size * 2
        val totalSize = 36 + dataSize
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalSize)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(1)
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2)
        buffer.putShort(2)
        buffer.putShort(16)
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)

        for (sample in samples) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }
}
