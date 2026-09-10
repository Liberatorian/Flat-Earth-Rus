package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R

@Composable
fun AmbientAudioEffect(enabled: Boolean, volume: Float) {
    val context = LocalContext.current
    val player = remember(context) { LicensedAmbientPlayer(context) }
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

private class LicensedAmbientPlayer(context: Context) {
    private val player = MediaPlayer.create(context, R.raw.upon_a_gray_day)
    private var volume = 0.35f

    init {
        player?.isLooping = true
        player?.setVolume(volume, volume)
    }

    fun setVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
        player?.setVolume(volume, volume)
    }

    fun start() {
        if (player?.isPlaying == false) player.start()
    }

    fun stop() {
        if (player?.isPlaying == true) player.pause()
    }
}
