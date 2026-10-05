package com.example

import com.example.model.GeoPoint
import com.example.model.NaturalEarthLandData
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NaturalEarthLandDataTest {
    @Test
    fun naturalEarthAssetContainsDetailedContinentsAndIslandRegions() {
        val polygons = NaturalEarthLandData.load(RuntimeEnvironment.getApplication())
        val allPoints = polygons.flatten()

        assertTrue("Expected Natural Earth 1:50m land polygons", polygons.size > 1_000)
        assertTrue("Expected detailed coastlines", allPoints.size > 50_000)
        assertTrue("Iceland should be represented", allPoints.hasPointNear(65.0, -19.0, 4.0))
        assertTrue("Svalbard should be represented", allPoints.hasPointNear(78.0, 18.0, 5.0))
        assertTrue("Madagascar should be represented", allPoints.hasPointNear(-20.0, 47.0, 5.0))
        assertTrue("New Zealand should be represented", allPoints.hasPointNear(-42.0, 173.0, 5.0))
    }

    private fun List<GeoPoint>.hasPointNear(latitude: Double, longitude: Double, tolerance: Double): Boolean =
        any { point ->
            kotlin.math.abs(point.lat - latitude) <= tolerance &&
                kotlin.math.abs(((point.lon - longitude + 540.0) % 360.0) - 180.0) <= tolerance
        }
}