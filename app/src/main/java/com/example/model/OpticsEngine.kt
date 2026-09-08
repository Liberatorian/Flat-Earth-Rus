package com.example.model

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

data class OpticalPathResult(
    val geometricAltitudeDeg: Double,
    val observedAltitudeDeg: Double,
    val refractionArcmin: Double,
    val opticalThickness: Double,
    val pathSamples: Int
)

/**
 * Minimal, inspectable atmosphere model for the observer renderer.
 * It uses a spherical exponential density profile and integrates the
 * direction change in small height steps instead of applying a screen warp.
 */
object OpticsEngine {
    private const val SURFACE_REFRACTIVE_INCREMENT = 2.9e-4
    private const val SCALE_HEIGHT_KM = 8.4
    private const val ATMOSPHERE_LIMIT_KM = 80.0
    private const val STEP_KM = 0.5

    fun traceIncomingRay(
        geometricAltitudeDeg: Double,
        observerHeightKm: Double = 0.002,
        aerosolFactor: Double = 1.0
    ): OpticalPathResult {
        val clampedAltitude = geometricAltitudeDeg.coerceIn(-89.0, 89.0)
        val altitudeRad = Math.toRadians(clampedAltitude)
        var rayAngle = altitudeRad
        var opticalThickness = 0.0
        var height = observerHeightKm.coerceAtLeast(0.0)
        var samples = 0

        while (height < ATMOSPHERE_LIMIT_KM && samples < 512) {
            val density = exp(-height / SCALE_HEIGHT_KM)
            val refractiveIncrement = SURFACE_REFRACTIVE_INCREMENT * density * aerosolFactor.coerceAtLeast(0.0)
            val localBending = refractiveIncrement * sin(rayAngle).coerceIn(-1.0, 1.0) * (STEP_KM / SCALE_HEIGHT_KM)
            rayAngle += localBending
            opticalThickness += density * STEP_KM
            height += STEP_KM
            samples += 1
        }

        val observedAltitude = Math.toDegrees(rayAngle)
        return OpticalPathResult(
            geometricAltitudeDeg = clampedAltitude,
            observedAltitudeDeg = observedAltitude,
            refractionArcmin = (observedAltitude - clampedAltitude) * 60.0,
            opticalThickness = opticalThickness,
            pathSamples = samples
        )
    }

    fun atmosphericVisibility(distanceKm: Double, humidity: Double = 0.55): Double {
        val aerosolDepth = (0.018 + humidity.coerceIn(0.0, 1.0) * 0.045) * distanceKm.coerceAtLeast(0.0)
        return exp(-aerosolDepth)
    }
}