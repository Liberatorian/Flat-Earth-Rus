package com.example

import com.example.model.CelestialEngine
import com.example.model.MapProjection
import com.example.model.OpticsEngine
import com.example.model.FlatEarthConstants
import com.example.model.GleasonMapData
import com.example.model.PRESET_CITIES
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

    @Test
    fun siderealDomeAdvancesAtAboutFifteenDegreesPerHour() {
        val hour = 3_600_000L
        val first = CelestialEngine.greenwichMeanSiderealTimeDegrees(0L)
        val second = CelestialEngine.greenwichMeanSiderealTimeDegrees(hour)
        val advance = (second - first + 360.0) % 360.0

        assertEquals(15.041, advance, 0.01)
    }

    @Test
    fun opticalPathReturnsSeparateObservedPositionAndFiniteAtmosphericDepth() {
        val path = OpticsEngine.traceIncomingRay(12.0)

        assertEquals(12.0, path.geometricAltitudeDeg, 0.0001)
        assertTrue(path.observedAltitudeDeg.isFinite())
        assertTrue(path.opticalThickness > 0.0)
        assertTrue(path.pathSamples > 0)
    }

    @Test
    fun selectableProjectionsProduceDifferentCoordinates() {
        val gleason = CelestialEngine.geoToDiscKm(45.0, 30.0, MapProjection.GLEASON_AE)
        val stereographic = CelestialEngine.geoToDiscKm(45.0, 30.0, MapProjection.STEREOGRAPHIC)
        val orthographic = CelestialEngine.geoToDiscKm(45.0, 30.0, MapProjection.ORTHOGRAPHIC)

        assertTrue(kotlin.math.hypot(gleason.first, gleason.second) != kotlin.math.hypot(stereographic.first, stereographic.second))
        assertTrue(kotlin.math.hypot(gleason.first, gleason.second) != kotlin.math.hypot(orthographic.first, orthographic.second))
    }

    @Test
    fun domeEnvelopeContainsSunAndMoonAlongTheirOrbitalBand() {
        for (radius in 3..7) {
            val radiusNorm = radius / 10.0
            val domeHeight = FlatEarthConstants.domeHeightAtRadiusKm(radiusNorm)
            assertTrue(domeHeight > FlatEarthConstants.SUN_ALTITUDE_KM)
            assertTrue(domeHeight > FlatEarthConstants.MOON_ALTITUDE_KM)
        }
    }

    @Test
    fun moonFollowsSunInSameDirectionButSlightlySlower() {
        val first = CelestialEngine.calculateState(1_700_000_000_000L, PRESET_CITIES[0])
        val second = CelestialEngine.calculateState(1_700_000_000_000L + 3_600_000L, PRESET_CITIES[0])
        val sunDelta = signedLongitudeDelta(first.sunLongitude, second.sunLongitude)
        val moonDelta = signedLongitudeDelta(first.moonLongitude, second.moonLongitude)

        assertTrue(sunDelta < 0.0)
        assertTrue(moonDelta < 0.0)
        assertTrue(kotlin.math.abs(moonDelta) < kotlin.math.abs(sunDelta))
    }

    @Test
    fun observerTelemetryContainsDistanceBasedAngularDiameters() {
        val state = CelestialEngine.calculateState(1_700_000_000_000L, PRESET_CITIES[0])

        assertTrue(state.sunApparentDiameterArcmin > 0.0)
        assertTrue(state.moonApparentDiameterArcmin > 0.0)
        assertTrue(state.observerDistanceToSunKm > 0.0)
        assertTrue(state.observerDistanceToMoonKm > 0.0)
    }

    @Test
    fun polarIceContinentIsPresentAtTheDiscCenter() {
        assertTrue(GleasonMapData.ALL_CONTINENTS.contains(GleasonMapData.POLAR_ICE_CONTINENT))
        val center = GleasonMapData.POLAR_ICE_CONTINENT.first()
        val projected = CelestialEngine.geoToDiscKm(center.lat, center.lon)
        assertTrue(kotlin.math.hypot(projected.first, projected.second) < 500.0)
    }

    private fun signedLongitudeDelta(first: Double, second: Double): Double {
        return ((second - first + 540.0) % 360.0) - 180.0
    }
}