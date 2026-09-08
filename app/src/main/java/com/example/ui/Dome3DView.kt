package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CONSTELLATION_LINES
import com.example.model.DomePhysicalSettings
import com.example.model.FlatEarthAppState
import com.example.model.FlatEarthConstants
import com.example.model.GleasonMapData
import com.example.model.MAJOR_STARS
import com.example.model.PRESET_CITIES
import com.example.ui.theme.EquatorGold
import com.example.ui.theme.GridCyan
import com.example.ui.theme.IceBlue
import com.example.ui.theme.IceWallCyan
import com.example.ui.theme.MoonSilver
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunGlow
import com.example.ui.theme.TropicRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D isometric/perspective projection of Flat Earth:
 * World Disk, Ice Wall, Crystalline Dome, Orbiting Sun and Moon with light cones,
 * star canopy rotating overhead, and observer marker.
 */
@Composable
fun Dome3DView(
    state: FlatEarthAppState,
    onCameraDelta: (pitchDelta: Float, yawDelta: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag X rotates azimuth (yaw), Drag Y changes pitch angle
                    val yawDelta = dragAmount.x * 0.3f
                    val pitchDelta = -dragAmount.y * 0.25f
                    onCameraDelta(pitchDelta, yawDelta)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.52f

            val baseRadius = (minOf(width, height) * 0.42f) * state.camera3D.zoom
            val pitchRad = Math.toRadians(state.camera3D.pitchDeg.toDouble())
            val yawRad = Math.toRadians(state.camera3D.yawDeg.toDouble())

            val cosPitch = cos(pitchRad).toFloat()
            val sinPitch = sin(pitchRad).toFloat()
            val cosYaw = cos(yawRad).toFloat()
            val sinYaw = sin(yawRad).toFloat()

            // 3D Projection helper: (x, y, z) on flat earth space where Z is UP (dome height)
            // X: East, Y: North, Z: Up (all normalized to [-1, 1] relative to disc radius)
            fun project3D(xNorm: Double, yNorm: Double, zNorm: Double): Offset {
                // 1. Rotate around Z axis by yaw
                val xRot = (xNorm * cosYaw - yNorm * sinYaw).toFloat()
                val yRot = (xNorm * sinYaw + yNorm * cosYaw).toFloat()

                // 2. Tilt by pitch (viewing from angle)
                // In screen space:
                // screenX = centerX + xRot * baseRadius
                // screenY = centerY - (yRot * cosPitch - zNorm * sinPitch) * baseRadius
                val screenX = centerX + xRot * baseRadius
                val screenY = centerY - (yRot * cosPitch + zNorm.toFloat() * sinPitch) * baseRadius
                return Offset(screenX, screenY)
            }

            // Draw Background deep space gradient & celestial stars
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712),
                        Color(0xFF070E22),
                        Color(0xFF0A1633)
                    )
                )
            )

            // 1. Draw Star Canopy on the Crystalline Firmament Dome
            if (state.layers.showStarsAndConstellations) {
                drawStarDome(
                    state = state,
                    project = ::project3D
                )
            }

            // 2. Draw Transparent Crystalline Firmament Dome (Glass Arcs & Zenith Apex)
            if (state.layers.showFirmamentGlow) {
                drawCrystallineDome(
                    project = ::project3D
                )
            }

            // 3. Draw The Flat Earth Disc (Oceans, Ice Wall, Grids)
            drawWorldDisc(
                state = state,
                project = ::project3D,
                cosPitch = cosPitch,
                baseRadius = baseRadius
            )

            // 4. Draw Continents on the Disc
            drawContinents3D(
                project = ::project3D
            )

            // 5. Draw Day / Night Spotlight Cone from Sun onto Disc
            if (state.layers.showDayNightCone) {
                drawDayNightSpotlight(
                    state = state,
                    project = ::project3D
                )
            }

            // 6. Draw City Markers
            if (state.layers.showCityMarkers) {
                drawCities3D(
                    project = ::project3D,
                    selectedCity = state.observerLocation
                )
            }

            // 7. Draw Observer Pin
            drawObserverPin(
                state = state,
                project = ::project3D
            )

            // 8. Draw Orbiting Sun with Spotlight Ray
            drawSun3D(
                state = state,
                project = ::project3D
            )

            // 9. Draw Orbiting Moon with Phases
            drawMoon3D(
                state = state,
                project = ::project3D
            )
        }

        // Camera hint overlay
        Surface(
            color = Color(0x660F172A),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "3D КУПОЛ (СНАРУЖИ) • Наклон: ${state.camera3D.pitchDeg.toInt()}° • Азимут: ${state.camera3D.yawDeg.toInt()}°\n(Перетаскивайте для свободного 3D-вращения)",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

private fun DrawScope.drawWorldDisc(
    state: FlatEarthAppState,
    project: (Double, Double, Double) -> Offset,
    cosPitch: Float,
    baseRadius: Float
) {
    val steps = 72

    // Outer Ice Wall Base Rim (-90° / r = 1.0)
    val iceWallBase = Path()
    val iceWallTop = Path()
    val discEdge = Path()

    // Keep the exaggerated wall visible, but derive it from the model setting
    // instead of silently turning the 75 m physical wall into 1,600 km.
    val iceWallHeightNorm = DomePhysicalSettings().iceWallHeightVisualKm / FlatEarthConstants.DISC_RADIUS_KM

    for (i in 0..steps) {
        val angle = i * (2.0 * PI / steps)
        val x = sin(angle)
        val y = cos(angle)

        val ptBase = project(x, y, 0.0)
        val ptTop = project(x, y, iceWallHeightNorm)

        if (i == 0) {
            iceWallBase.moveTo(ptBase.x, ptBase.y)
            iceWallTop.moveTo(ptTop.x, ptTop.y)
            discEdge.moveTo(ptBase.x, ptBase.y)
        } else {
            iceWallBase.lineTo(ptBase.x, ptBase.y)
            iceWallTop.lineTo(ptTop.x, ptTop.y)
            discEdge.lineTo(ptBase.x, ptBase.y)
        }
    }

    // Disc Ocean Body fill
    drawPath(
        path = discEdge,
        color = Color(0xFF091428)
    )

    // Great Antarctic Ice Wall (3D translucent ring around the entire perimeter)
    for (i in 0 until steps) {
        val a1 = i * (2.0 * PI / steps)
        val a2 = (i + 1) * (2.0 * PI / steps)

        val b1 = project(sin(a1), cos(a1), 0.0)
        val b2 = project(sin(a2), cos(a2), 0.0)
        val t1 = project(sin(a1), cos(a1), iceWallHeightNorm)
        val t2 = project(sin(a2), cos(a2), iceWallHeightNorm)

        val quad = Path().apply {
            moveTo(b1.x, b1.y)
            lineTo(b2.x, b2.y)
            lineTo(t2.x, t2.y)
            lineTo(t1.x, t1.y)
            close()
        }

        // Shading based on orientation
        val alpha = (0.25f + 0.25f * sin(a1).toFloat()).coerceIn(0.2f, 0.6f)
        drawPath(
            path = quad,
            color = Color(0xFF7DD3FC).copy(alpha = alpha)
        )
    }

    // Ice Wall Crest Line
    drawPath(
        path = iceWallTop,
        color = Color(0xFFE0F2FE),
        style = Stroke(width = 2.5f)
    )

    // Coordinate Circles on Disc
    if (state.layers.showTropicsAndEquator) {
        drawDiscCircle(FlatEarthConstants.TROPIC_CANCER_KM / FlatEarthConstants.DISC_RADIUS_KM, TropicRed, 2.0f, true, project)
        drawDiscCircle(FlatEarthConstants.EQUATOR_RADIUS_KM / FlatEarthConstants.DISC_RADIUS_KM, EquatorGold, 2.5f, false, project)
        drawDiscCircle(FlatEarthConstants.TROPIC_CAPRICORN_KM / FlatEarthConstants.DISC_RADIUS_KM, TropicRed, 2.0f, true, project)
        drawDiscCircle(FlatEarthConstants.ANTARCTIC_CIRCLE_KM / FlatEarthConstants.DISC_RADIUS_KM, IceBlue.copy(alpha = 0.7f), 1.8f, true, project)
    }

    // Longitude Meridians (every 30 degrees = 2 hours)
    if (state.layers.showCoordinateGrid) {
        for (m in 0 until 12) {
            val angle = m * (PI / 6.0)
            val pNorth = project(0.0, 0.0, 0.0)
            val pOuter = project(sin(angle), cos(angle), 0.0)
            val pOuterOpp = project(-sin(angle), -cos(angle), 0.0)

            drawLine(
                color = GridCyan.copy(alpha = 0.25f),
                start = pOuterOpp,
                end = pOuter,
                strokeWidth = 1.0f
            )
        }
    }
}

private fun DrawScope.drawDiscCircle(
    radiusNorm: Double,
    color: Color,
    width: Float,
    dashed: Boolean,
    project: (Double, Double, Double) -> Offset
) {
    val path = Path()
    val steps = 64
    for (i in 0..steps) {
        val angle = i * (2.0 * PI / steps)
        val x = radiusNorm * sin(angle)
        val y = radiusNorm * cos(angle)
        val pt = project(x, y, 0.0)
        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = width,
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null
        )
    )
}

private fun DrawScope.drawContinents3D(
    project: (Double, Double, Double) -> Offset
) {
    for (continent in GleasonMapData.ALL_CONTINENTS) {
        val path = Path()
        for (i in continent.indices) {
            val pt = continent[i]
            val (xKm, yKm) = com.example.model.CelestialEngine.geoToDiscKm(pt.lat, pt.lon)
            val xNorm = xKm / FlatEarthConstants.DISC_RADIUS_KM
            val yNorm = yKm / FlatEarthConstants.DISC_RADIUS_KM
            val screenPt = project(xNorm, yNorm, 0.005)
            if (i == 0) path.moveTo(screenPt.x, screenPt.y) else path.lineTo(screenPt.x, screenPt.y)
        }
        path.close()

        // Continent fill: lush emerald-navy landmass
        drawPath(path = path, color = Color(0xFF1E3A2F))
        // Coastline stroke
        drawPath(path = path, color = Color(0xFF34D399), style = Stroke(width = 1.2f))
    }
}

private fun DrawScope.drawDayNightSpotlight(
    state: FlatEarthAppState,
    project: (Double, Double, Double) -> Offset
) {
    // Sun illuminates a circular cone on the plane
    val sunX = state.telemetry.sunDiscX
    val sunY = state.telemetry.sunDiscY
    val spotRadiusNorm = FlatEarthConstants.SUN_SPOTLIGHT_RADIUS_KM / FlatEarthConstants.DISC_RADIUS_KM

    val spotPath = Path()
    val steps = 48
    for (i in 0..steps) {
        val angle = i * (2.0 * PI / steps)
        val px = sunX + spotRadiusNorm * sin(angle)
        val py = sunY + spotRadiusNorm * cos(angle)
        val pt = project(px, py, 0.008)
        if (i == 0) spotPath.moveTo(pt.x, pt.y) else spotPath.lineTo(pt.x, pt.y)
    }
    spotPath.close()

    // Luminous daylight glow pool on Earth
    drawPath(
        path = spotPath,
        color = Color(0x35FDE047)
    )
    drawPath(
        path = spotPath,
        color = Color(0x60F59E0B),
        style = Stroke(width = 2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
    )
}

private fun DrawScope.drawCrystallineDome(
    project: (Double, Double, Double) -> Offset
) {
    val domeHeightNorm = FlatEarthConstants.DOME_ZENITH_HEIGHT_KM / FlatEarthConstants.DISC_RADIUS_KM // ~0.325
    val rimHeightNorm = FlatEarthConstants.DOME_RIM_HEIGHT_KM / FlatEarthConstants.DISC_RADIUS_KM

    // Meridian Dome Ribs (transparent crystal arches from North Pole zenith to Ice Rim)
    val ribs = 8
    val steps = 30
    for (r in 0 until ribs) {
        val ribAngle = r * (PI / ribs)
        val ribPath = Path()

        for (i in -steps..steps) {
            val t = i.toDouble() / steps // -1.0 .. +1.0
            val rad = kotlin.math.abs(t)
            // Parabolic dome curvature: z = H_rim + (H_zenith - H_rim) * (1 - rad^2)
            val z = rimHeightNorm + (domeHeightNorm - rimHeightNorm) * (1.0 - rad * rad)
            val x = t * sin(ribAngle)
            val y = t * cos(ribAngle)

            val pt = project(x, y, z)
            if (i == -steps) ribPath.moveTo(pt.x, pt.y) else ribPath.lineTo(pt.x, pt.y)
        }

        drawPath(
            path = ribPath,
            color = Color(0x3038BDF8),
            style = Stroke(width = 1.0f)
        )
    }

    // Latitude rings on the Dome
    val rings = listOf(0.3, 0.6, 0.85)
    for (radiusNorm in rings) {
        val ringPath = Path()
        val z = rimHeightNorm + (domeHeightNorm - rimHeightNorm) * (1.0 - radiusNorm * radiusNorm)
        val circleSteps = 48
        for (i in 0..circleSteps) {
            val a = i * (2.0 * PI / circleSteps)
            val x = radiusNorm * sin(a)
            val y = radiusNorm * cos(a)
            val pt = project(x, y, z)
            if (i == 0) ringPath.moveTo(pt.x, pt.y) else ringPath.lineTo(pt.x, pt.y)
        }
        drawPath(
            path = ringPath,
            color = Color(0x2238BDF8),
            style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        )
    }

    // Zenith apex glow (Polaris position)
    val apexPt = project(0.0, 0.0, domeHeightNorm)
    drawCircle(
        color = Color(0xFF67E8F9),
        radius = 5.0f,
        center = apexPt
    )
    drawCircle(
        color = Color(0x4038BDF8),
        radius = 12.0f,
        center = apexPt
    )
}

private fun DrawScope.drawStarDome(
    state: FlatEarthAppState,
    project: (Double, Double, Double) -> Offset
) {
    val domeHeightNorm = FlatEarthConstants.DOME_ZENITH_HEIGHT_KM / FlatEarthConstants.DISC_RADIUS_KM
    val rimHeightNorm = FlatEarthConstants.DOME_RIM_HEIGHT_KM / FlatEarthConstants.DISC_RADIUS_KM
    val rotationRad = Math.toRadians(state.telemetry.starDomeRotationDeg.toDouble())

    // Cache star screen positions for constellation lines
    val starScreenPos = mutableMapOf<String, Offset>()

    for (star in MAJOR_STARS) {
        // Declination mapped to radial distance on celestial sphere:
        // Dec +90 (Polaris) -> r = 0 (Apex)
        // Dec 0 (Celestial Equator) -> r = 0.5
        // Dec -80 -> r = 0.94
        val starRadNorm = ((90.0 - star.declinationDeg) / 180.0).coerceIn(0.0, 0.95)

        // Right ascension + sidereal dome rotation
        val starHourAngle = (star.rightAscensionHours / 24.0 * 2.0 * PI) + rotationRad
        val x = starRadNorm * sin(starHourAngle)
        val y = starRadNorm * cos(starHourAngle)
        val z = rimHeightNorm + (domeHeightNorm - rimHeightNorm) * (1.0 - starRadNorm * starRadNorm)

        val pt = project(x, y, z)
        starScreenPos[star.name] = pt

        // Draw star dot with magnitude sizing
        val starRadius = (3.5f - star.magnitude * 0.7f).coerceIn(1.5f, 5.5f)
        val starColor = when {
            star.name.contains("Polaris") -> Color(0xFFFDE047)
            star.name.contains("Betelgeuse") -> Color(0xFFFB923C)
            star.name.contains("Sirius") || star.name.contains("Vega") -> Color(0xFFBAE6FD)
            else -> Color(0xFFFFFFFF)
        }

        drawCircle(
            color = starColor.copy(alpha = 0.85f),
            radius = starRadius,
            center = pt
        )
    }

    // Draw Constellation connecting lines
    for (line in CONSTELLATION_LINES) {
        val p1 = starScreenPos[line.star1]
        val p2 = starScreenPos[line.star2]
        if (p1 != null && p2 != null) {
            drawLine(
                color = Color(0x3538BDF8),
                start = p1,
                end = p2,
                strokeWidth = 1.0f
            )
        }
    }
}

private fun DrawScope.drawSun3D(
    state: FlatEarthAppState,
    project: (Double, Double, Double) -> Offset
) {
    val sunAltNorm = FlatEarthConstants.SUN_ALTITUDE_KM / FlatEarthConstants.DISC_RADIUS_KM // ~0.25
    val sunX = state.telemetry.sunDiscX
    val sunY = state.telemetry.sunDiscY

    val sunScreenPt = project(sunX, sunY, sunAltNorm)
    val groundPt = project(sunX, sunY, 0.0)

    // Volumetric Sunbeam pointing down to earth
    if (state.layers.showSunBeam) {
        val beamPath = Path().apply {
            val spotRadius = (FlatEarthConstants.SUN_SPOTLIGHT_RADIUS_KM * 0.7 / FlatEarthConstants.DISC_RADIUS_KM)
            val gLeft = project(sunX - spotRadius * 0.5, sunY, 0.0)
            val gRight = project(sunX + spotRadius * 0.5, sunY, 0.0)

            moveTo(sunScreenPt.x, sunScreenPt.y)
            lineTo(gLeft.x, gLeft.y)
            lineTo(gRight.x, gRight.y)
            close()
        }
        drawPath(
            path = beamPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x60FDE047), Color(0x10FDE047)),
                startY = sunScreenPt.y,
                endY = groundPt.y
            )
        )

        // Subsolar vertical tether line
        drawLine(
            color = Color(0x80FDE047),
            start = sunScreenPt,
            end = groundPt,
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    }

    // Solar Corona & Radiant Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFEE58), Color(0x80FFB300), Color(0x00FF8F00)),
            center = sunScreenPt,
            radius = 28f
        ),
        radius = 28f,
        center = sunScreenPt
    )

    // Sun Core Sphere
    drawCircle(
        color = SunGold,
        radius = 10f,
        center = sunScreenPt
    )
}

private fun DrawScope.drawMoon3D(
    state: FlatEarthAppState,
    project: (Double, Double, Double) -> Offset
) {
    val moonAltNorm = FlatEarthConstants.MOON_ALTITUDE_KM / FlatEarthConstants.DISC_RADIUS_KM
    val moonX = state.telemetry.moonDiscX
    val moonY = state.telemetry.moonDiscY

    val moonScreenPt = project(moonX, moonY, moonAltNorm)
    val groundPt = project(moonX, moonY, 0.0)

    // Moon tether line to ground
    if (state.layers.showMoonBeam) {
        drawLine(
            color = Color(0x60E2E8F0),
            start = moonScreenPt,
            end = groundPt,
            strokeWidth = 1.0f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    }

    // Moon soft silver glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x90F1F5F9), Color(0x3094A3B8), Color(0x000F172A)),
            center = moonScreenPt,
            radius = 20f
        ),
        radius = 20f,
        center = moonScreenPt
    )

    // Moon Disc with Phase representation
    val moonRadius = 8f
    // Draw Dark Side base
    drawCircle(
        color = Color(0xFF1E293B),
        radius = moonRadius,
        center = moonScreenPt
    )

    // Draw illuminated crescent or gibbous facing towards the Sun
    val phaseFrac = state.telemetry.moonPhaseFraction
    if (phaseFrac > 0.05f) {
        drawCircle(
            color = MoonSilver,
            radius = moonRadius * phaseFrac.coerceIn(0.2f, 1.0f),
            center = moonScreenPt
        )
    }
}

private fun DrawScope.drawCities3D(
    project: (Double, Double, Double) -> Offset,
    selectedCity: com.example.model.CityLocation
) {
    for (city in PRESET_CITIES) {
        if (city.nameRu == selectedCity.nameRu) continue
        val (xKm, yKm) = com.example.model.CelestialEngine.geoToDiscKm(city.latitude, city.longitude)
        val xNorm = xKm / FlatEarthConstants.DISC_RADIUS_KM
        val yNorm = yKm / FlatEarthConstants.DISC_RADIUS_KM
        val pt = project(xNorm, yNorm, 0.01)

        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.7f),
            radius = 3.0f,
            center = pt
        )
    }
}

private fun DrawScope.drawObserverPin(
    state: FlatEarthAppState,
    project: (Double, Double, Double) -> Offset
) {
    val obsX = state.telemetry.observerDiscX
    val obsY = state.telemetry.observerDiscY
    val pt = project(obsX, obsY, 0.015)

    // Pulsing beacon ring
    val pulseRadius = 12f + 4f * sin((System.currentTimeMillis() % 2000) / 2000.0 * 2 * PI).toFloat()
    drawCircle(
        color = Color(0x5022D3EE),
        radius = pulseRadius,
        center = pt
    )

    // Observer Pin Point
    drawCircle(
        color = Color(0xFF06B6D4),
        radius = 5.0f,
        center = pt
    )
    drawCircle(
        color = Color.White,
        radius = 2.0f,
        center = pt
    )
}
