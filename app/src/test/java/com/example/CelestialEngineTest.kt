package com.example

import com.example.model.CelestialEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CelestialEngineTest {
    @Test
    fun discCoordinatesKeepEquatorAndPoleAtExpectedRadii() {
        val pole = CelestialEngine.geoToDiscKm(90.0, 0.0)
        val equator = CelestialEngine.geoToDiscKm(0.0, 0.0)
        val rim = CelestialEngine.geoToDiscKm(-90.0, 0.0)

        assertEquals(0.0, kotlin.math.hypot(pole.first, pole.second), 0.001)
        assertEquals(10_000.0, kotlin.math.hypot(equator.first, equator.second), 0.001)
        assertEquals(20_000.0, kotlin.math.hypot(rim.first, rim.second), 0.001)
    }

    @Test
    fun siderealClockIsBoundedAndAdvancesByAboutFourMinutesPerSolarDay() {
        val day = 86_400_000L
        val first = CelestialEngine.greenwichMeanSiderealTimeDegrees(0L)
        val second = CelestialEngine.greenwichMeanSiderealTimeDegrees(day)
        val advance = (second - first + 360.0) % 360.0

        assertTrue(first >= 0.0 && first < 360.0)
        assertEquals(0.9856, advance, 0.01)
    }
}