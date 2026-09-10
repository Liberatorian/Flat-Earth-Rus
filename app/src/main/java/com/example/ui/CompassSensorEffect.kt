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
import kotlin.math.asin
import kotlin.math.atan2

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

        // The rear camera looks along the device -Z axis. Transform that axis
        // into the world frame instead of using the phone body's pitch, which
        // makes a vertically held phone incorrectly point at the zenith.
        val cameraEast = -remappedMatrix[2].toDouble()
        val cameraNorth = -remappedMatrix[5].toDouble()
        val cameraUp = (-remappedMatrix[8].toDouble()).coerceIn(-1.0, 1.0)
        val azimuth = Math.toDegrees(atan2(cameraEast, cameraNorth))
            .let { ((it + 360.0) % 360.0).toFloat() }
        val elevation = Math.toDegrees(asin(cameraUp)).toFloat().coerceIn(0f, 90f)
        onOrientationChanged(azimuth, elevation)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}