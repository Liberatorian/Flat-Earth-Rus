package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
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
    private val context: Context,
    private val onOrientationChanged: (Float, Float) -> Unit
) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
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
        val displayRotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.rotation
        val (axisX, axisY) = when (displayRotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedMatrix)
        SensorManager.getOrientation(remappedMatrix, orientation)

        val azimuth = ((orientation[0] * 180f / PI.toFloat()) + 360f) % 360f
        // Android's positive pitch points opposite to the camera tilt expected
        // by the sky view: lowering the phone must lower the horizon.
        val pitch = (-orientation[1] * 180f / PI.toFloat()).coerceIn(-90f, 90f)
        onOrientationChanged(azimuth, pitch.coerceAtLeast(0f))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}