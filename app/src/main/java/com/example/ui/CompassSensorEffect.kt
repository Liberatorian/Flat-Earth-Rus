package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI

@Composable
fun CompassSensorEffect(
    enabled: Boolean,
    onOrientationChanged: (azimuthDeg: Float, elevationDeg: Float) -> Unit
) {
    val context = LocalContext.current
    val sensorController = remember(context) { CompassSensorController(context, onOrientationChanged) }

    DisposableEffect(enabled) {
        if (enabled) sensorController.start() else sensorController.stop()
        onDispose { sensorController.stop() }
    }
}

private class CompassSensorController(
    context: Context,
    private val onOrientationChanged: (Float, Float) -> Unit
) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    fun start() {
        rotationSensor?.let {
            manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        manager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            rotationMatrix
        )
        SensorManager.getOrientation(rotationMatrix, orientation)

        val azimuth = ((orientation[0] * 180f / PI.toFloat()) + 360f) % 360f
        val pitch = (orientation[1] * 180f / PI.toFloat()).coerceIn(-90f, 90f)
        onOrientationChanged(azimuth, pitch.coerceAtLeast(0f))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}