package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class GeoPoint(val lat: Double, val lon: Double)

/**
 * Simplified geographical shoreline polygons for the Azimuthal Equidistant Flat Earth map.
 * Points are given in standard (Latitude, Longitude) coordinates,
 * which are projected via Gleason's transformation to the disc plane.
 */
object GleasonMapData {

    // Eurasia coastline points
    val EURASIA = listOf(
        GeoPoint(70.0, 20.0), // Norway
        GeoPoint(75.0, 60.0), // Novaya Zemlya / Yamal
        GeoPoint(73.0, 110.0), // Taymyr
        GeoPoint(70.0, 180.0), // Chukotka
        GeoPoint(60.0, 160.0), // Kamchatka
        GeoPoint(50.0, 140.0), // Sea of Okhotsk
        GeoPoint(40.0, 125.0), // Korea / Liaodong
        GeoPoint(30.0, 120.0), // East China
        GeoPoint(20.0, 110.0), // South China / Hainan
        GeoPoint(10.0, 105.0), // Indochina
        GeoPoint(1.0, 104.0), // Singapore / Malaya
        GeoPoint(15.0, 95.0), // Myanmar
        GeoPoint(22.0, 90.0), // Bengal
        GeoPoint(8.0, 77.0), // South India (Kanyakumari)
        GeoPoint(25.0, 65.0), // Indus / Pakistan
        GeoPoint(25.0, 55.0), // Persian Gulf
        GeoPoint(12.0, 45.0), // Yemen / Aden
        GeoPoint(25.0, 35.0), // Red Sea
        GeoPoint(32.0, 35.0), // Levant
        GeoPoint(41.0, 28.0), // Bosporus / Black Sea
        GeoPoint(38.0, 23.0), // Greece
        GeoPoint(42.0, 15.0), // Italy
        GeoPoint(36.0, -5.0), // Gibraltar
        GeoPoint(43.0, -9.0), // Iberia (Galicia)
        GeoPoint(48.0, -4.0), // Brittany
        GeoPoint(55.0, 8.0), // Denmark
        GeoPoint(60.0, 5.0), // Bergen Norway
        GeoPoint(70.0, 20.0) // Close loop
    )

    // British Isles
    val BRITAIN = listOf(
        GeoPoint(50.0, -5.0),
        GeoPoint(52.0, 1.5),
        GeoPoint(58.0, -3.0),
        GeoPoint(56.0, -6.0),
        GeoPoint(50.0, -5.0)
    )

    // Africa coastline
    val AFRICA = listOf(
        GeoPoint(36.0, -5.0), // Gibraltar / Morocco
        GeoPoint(37.0, 10.0), // Tunisia
        GeoPoint(32.0, 20.0), // Libya
        GeoPoint(31.0, 32.0), // Egypt / Delta
        GeoPoint(25.0, 35.0), // Red Sea
        GeoPoint(12.0, 51.0), // Horn of Africa (Somalia)
        GeoPoint(-5.0, 40.0), // Kenya / Tanzania
        GeoPoint(-25.0, 35.0), // Mozambique
        GeoPoint(-34.0, 25.0), // South Africa (Port Elizabeth)
        GeoPoint(-34.5, 19.0), // Cape of Good Hope
        GeoPoint(-22.0, 14.0), // Namibia
        GeoPoint(-5.0, 12.0), // Congo / Gabon
        GeoPoint(4.0, 7.0), // Niger Delta
        GeoPoint(5.0, -3.0), // Ivory Coast
        GeoPoint(15.0, -17.0), // Senegal / Dakar
        GeoPoint(24.0, -15.0), // Western Sahara
        GeoPoint(36.0, -5.0)
    )

    // North America coastline
    val NORTH_AMERICA = listOf(
        GeoPoint(71.0, -156.0), // Point Barrow, Alaska
        GeoPoint(65.0, -168.0), // Bering Strait
        GeoPoint(58.0, -160.0), // Alaska Peninsula
        GeoPoint(55.0, -132.0), // Panhandle
        GeoPoint(48.0, -124.0), // Seattle / Pacific NW
        GeoPoint(34.0, -120.0), // California
        GeoPoint(23.0, -110.0), // Baja California tip
        GeoPoint(16.0, -95.0), // Mexico Pacific
        GeoPoint(9.0, -80.0), // Panama
        GeoPoint(18.0, -88.0), // Yucatan
        GeoPoint(28.0, -96.0), // Texas Gulf
        GeoPoint(25.0, -81.0), // Florida
        GeoPoint(35.0, -75.0), // Cape Hatteras
        GeoPoint(43.0, -70.0), // Boston / Maine
        GeoPoint(47.0, -53.0), // Newfoundland
        GeoPoint(60.0, -64.0), // Labrador
        GeoPoint(70.0, -85.0), // Baffin Island
        GeoPoint(72.0, -125.0), // Banks Island
        GeoPoint(71.0, -156.0)
    )

    // Greenland
    val GREENLAND = listOf(
        GeoPoint(60.0, -45.0),
        GeoPoint(70.0, -22.0),
        GeoPoint(82.0, -30.0),
        GeoPoint(80.0, -60.0),
        GeoPoint(70.0, -55.0),
        GeoPoint(60.0, -45.0)
    )

    // South America coastline
    val SOUTH_AMERICA = listOf(
        GeoPoint(9.0, -80.0), // Colombia / Panama border
        GeoPoint(11.0, -74.0), // Caribbean coast
        GeoPoint(10.0, -62.0), // Venezuela / Trinidad
        GeoPoint(5.0, -52.0), // French Guiana
        GeoPoint(-5.0, -35.0), // Natal / Brazil Horn
        GeoPoint(-23.0, -43.0), // Rio de Janeiro
        GeoPoint(-35.0, -55.0), // Rio de la Plata
        GeoPoint(-52.0, -68.0), // Patagonia (Atlantic)
        GeoPoint(-55.0, -67.0), // Cape Horn / Tierra del Fuego
        GeoPoint(-45.0, -75.0), // Chilean Fjords
        GeoPoint(-20.0, -70.0), // Iquique, Chile
        GeoPoint(-5.0, -81.0), // Peru (Pariñas)
        GeoPoint(1.0, -79.0), // Ecuador
        GeoPoint(9.0, -80.0)
    )

    // Australia
    val AUSTRALIA = listOf(
        GeoPoint(-11.0, 142.0), // Cape York
        GeoPoint(-17.0, 146.0), // Cairns / Great Barrier
        GeoPoint(-28.0, 153.0), // Brisbane
        GeoPoint(-38.0, 145.0), // Melbourne
        GeoPoint(-35.0, 137.0), // Adelaide
        GeoPoint(-34.0, 115.0), // Southwest Cape
        GeoPoint(-22.0, 114.0), // North West Cape
        GeoPoint(-15.0, 125.0), // Kimberley
        GeoPoint(-12.0, 131.0), // Darwin
        GeoPoint(-15.0, 136.0), // Gulf of Carpentaria
        GeoPoint(-11.0, 142.0)
    )

    // Major Islands: Japan, Madagascar, New Zealand
    val JAPAN = listOf(
        GeoPoint(45.0, 142.0),
        GeoPoint(36.0, 140.0),
        GeoPoint(31.0, 131.0),
        GeoPoint(35.0, 133.0),
        GeoPoint(45.0, 142.0)
    )

    val MADAGASCAR = listOf(
        GeoPoint(-12.0, 49.0),
        GeoPoint(-25.0, 47.0),
        GeoPoint(-25.0, 44.0),
        GeoPoint(-16.0, 44.0),
        GeoPoint(-12.0, 49.0)
    )

    val ALL_CONTINENTS = listOf(
        EURASIA,
        BRITAIN,
        AFRICA,
        NORTH_AMERICA,
        GREENLAND,
        SOUTH_AMERICA,
        AUSTRALIA,
        JAPAN,
        MADAGASCAR
    )

    /**
     * Converts a polygon of geo points into a Compose Path scaled to canvas center and radius.
     */
    fun polygonToPath(
        points: List<GeoPoint>,
        centerX: Float,
        centerY: Float,
        discRadiusPx: Float,
        projection: MapProjection = MapProjection.GLEASON_AE
    ): Path {
        val path = Path()
        if (points.isEmpty()) return path

        val projected = points.map { point ->
            val (xKm, yKm) = CelestialEngine.geoToDiscKm(point.lat, point.lon, projection)
            Offset(
                centerX + (xKm / FlatEarthConstants.DISC_RADIUS_KM * discRadiusPx).toFloat(),
                centerY + (yKm / FlatEarthConstants.DISC_RADIUS_KM * discRadiusPx).toFloat()
            )
        }
        if (projected.size == 1) {
            path.moveTo(projected[0].x, projected[0].y)
            return path
        }

        // Midpoint quadratic smoothing keeps the rough demo coastline readable
        // without changing its projected geographic anchor points.
        fun midpoint(first: Offset, second: Offset) = Offset(
            (first.x + second.x) * 0.5f,
            (first.y + second.y) * 0.5f
        )

        val firstMidpoint = midpoint(projected.last(), projected.first())
        path.moveTo(firstMidpoint.x, firstMidpoint.y)
        for (i in projected.indices) {
            val current = projected[i]
            val next = projected[(i + 1) % projected.size]
            val nextMidpoint = midpoint(current, next)
            path.quadraticTo(
                current.x,
                current.y,
                nextMidpoint.x,
                nextMidpoint.y
            )
        }
        path.close()
        return path
    }
}
