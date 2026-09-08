package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

@Composable
fun AmbientAudioEffect(enabled: Boolean) {
    val player = remember { ProceduralAmbientPlayer() }

    DisposableEffect(enabled) {
        if (enabled) player.start() else player.stop()
        onDispose { player.stop() }
    }
}

/** Original synthesized drone: no bundled recording and no external license. */
private class ProceduralAmbientPlayer {
    private val sampleRate = 22_050
    private val frameCount = sampleRate * 2
    private var audioTrack: AudioTrack? = null
    private var worker: Thread? = null

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
        worker = Thread {
            val buffer = ShortArray(frameCount)
            var phase = 0.0
            track.play()
            while (!Thread.currentThread().isInterrupted) {
                for (index in buffer.indices) {
                    val time = phase + index.toDouble() / sampleRate
                    val value =
                        kotlin.math.sin(time * Math.PI * 2.0 * 110.0) * 0.08 +
                            kotlin.math.sin(time * Math.PI * 2.0 * 164.81) * 0.035 +
                            kotlin.math.sin(time * Math.PI * 2.0 * 220.0) * 0.018
                    buffer[index] = (value * Short.MAX_VALUE).toInt().toShort()
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