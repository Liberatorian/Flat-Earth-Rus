package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AmbientAudioEffect(enabled: Boolean, volume: Float) {
    val player = remember { ProceduralAmbientPlayer() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(volume) {
        player.setVolume(volume)
    }

    DisposableEffect(lifecycleOwner, enabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (enabled) player.start()
                Lifecycle.Event.ON_STOP -> player.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (enabled && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            player.start()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.stop()
        }
    }
}

/** Original synthesized ambient soundscape: no bundled recording and no external license. */
private class ProceduralAmbientPlayer {
    private val sampleRate = 22_050
    private val frameCount = sampleRate * 2
    private var audioTrack: AudioTrack? = null
    private var worker: Thread? = null
    private var volume = 0.35f

    fun setVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
        audioTrack?.setVolume(volume)
    }

    fun start() {
        if (worker?.isAlive == true) return
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuffer, frameCount * 2))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.setVolume(volume)
        worker = Thread {
            val buffer = ShortArray(frameCount)
            var phase = 0.0
            var noiseState = 0.0
            val random = Random(1937)
            track.play()
            while (!Thread.currentThread().isInterrupted) {
                for (index in buffer.indices) {
                    val time = phase + index.toDouble() / sampleRate
                    val breath = 0.5 + 0.5 * sin(time * PI * 2.0 / 13.0)
                    val low = sin(time * PI * 2.0 * 72.0) * (0.018 + breath * 0.024)
                    val fifth = sin(time * PI * 2.0 * 108.0 + sin(time / 7.0) * 0.7) * 0.014
                    val shimmerGate = (sin(time * PI * 2.0 / 5.7) + 1.0) * 0.5
                    val shimmer = sin(time * PI * 2.0 * 880.0) * shimmerGate * 0.004
                    noiseState = noiseState * 0.985 + (random.nextDouble() * 2.0 - 1.0) * 0.015
                    val value = low + fifth + shimmer + noiseState * (0.012 + breath * 0.01)
                    buffer[index] = (value.coerceIn(-0.8, 0.8) * Short.MAX_VALUE).toInt().toShort()
                }
                track.write(buffer, 0, buffer.size)
                phase += buffer.size.toDouble() / sampleRate
            }
        }.also { it.start() }
    }

    fun stop() {
        worker?.interrupt()
        worker = null
        audioTrack?.run {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) stop()
            release()
        }
        audioTrack = null
    }
}