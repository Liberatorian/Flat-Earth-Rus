package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CONSTELLATION_LINES
import com.example.model.CelestialEngine
import com.example.model.FlatEarthAppState
import com.example.model.MAJOR_STARS
import com.example.ui.theme.MoonSilver
import com.example.ui.theme.SunGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Inside-the-Dome perspective: Observer standing on the disk at selected location
 * looking up at the sky dome with local horizon, compass azimuth, elevation rings,
 * perspective solar/lunar movement, and rotating star dome.
 */
@Composable
fun ObserverSkyView(
    state: FlatEarthAppState,
    onCameraDelta: (azimuthDelta: Float, elevationDelta: Float, zoomDelta: Float) -> Unit,
    onSetCamera: (azimuth: Float, elevation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    // Pan rotates the view; pinch changes the field of view.
                    val azDelta = -pan.x * 0.25f + rotation * 0.15f
                    val elDelta = -pan.y * 0.2f
                    onCameraDelta(azDelta, elDelta, zoom - 1f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.55f
            val skyRadius = minOf(width, height) * 0.44f * state.observerCamera.fovZoom

            val isDay = state.telemetry.isObserverDaytime
            val sunAlt = state.telemetry.sunApparentAltitudeDeg.toFloat()

            // 1. Dynamic Atmosphere background based on daylight & sun altitude
            val skyColors = when {
                isDay && sunAlt > 25f -> listOf(
                    Color(0xFF0284C7), // Bright Azure Zenith
                    Color(0xFF38BDF8),
                    Color(0xFFBAE6FD) // Bright Horizon
                )
                isDay && sunAlt > 5f -> listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E3A8A),
                    Color(0xFFF59E0B) // Golden Golden Hour
                )
                isDay -> listOf(
                    Color(0xFF090D1A),
                    Color(0xFF1E1B4B),
                    Color(0xFFEA580C) // Twilight Red Horizon
                )
                else -> listOf(
                    Color(0xFF030712), // Deep Space Midnight
                    Color(0xFF0B1229),
                    Color(0xFF132042)
                )
            }

            drawRect(
                brush = Brush.verticalGradient(
                    colors = skyColors,
                    startY = 0f,
                    endY = height
                )
            )

            // Local Projection helper: converts local (AzimuthDeg, AltitudeDeg)
            // into screen coordinate (x, y) centered around observer's current look heading
            // Looking at lookAzimuth and lookElevation
            val lookAz = state.observerCamera.azimuthHeadingDeg
            val lookEl = state.observerCamera.elevationPitchDeg

            // Orthographic projection onto the observer's view plane. The camera
            // elevation affects both the visible hemisphere and star positions.
            fun skyToScreen(targetAz: Double, targetAlt: Double): Offset? {
                val targetAltRad = Math.toRadians(targetAlt)
                val lookElRad = Math.toRadians(lookEl.toDouble())
                val deltaAzRad = Math.toRadians(targetAz - lookAz)
                val sinAlt = sin(targetAltRad)
                val cosAlt = cos(targetAltRad)
                val forward = sinAlt * sin(lookElRad) + cosAlt * cos(deltaAzRad) * cos(lookElRad)
                if (forward <= 0.0) return null

                val right = cosAlt * sin(deltaAzRad)
                val up = sinAlt * cos(lookElRad) - cosAlt * cos(deltaAzRad) * sin(lookElRad)
                return Offset(
                    centerX + (right * skyRadius).toFloat(),
                    centerY - (up * skyRadius).toFloat()
                )
            }

            // 2. Draw Horizon & Elevation Grid Rings
            drawElevationGrid(
                centerX = centerX,
                centerY = centerY,
                skyRadius = skyRadius,
                lookAz = lookAz,
                lookEl = lookEl
            )

            // 3. Draw Stars & Constellations in Local Sky
            if (!isDay || sunAlt < 8f) {
                drawLocalStars(
                    state = state,
                    skyToScreen = ::skyToScreen
                )
            }

            // 4. Draw The Sun with Perspective Glare and Ray
            drawLocalSun(
                state = state,
                skyToScreen = ::skyToScreen
            )

            // 5. Draw The Moon with Apparent Phase and Orientation
            drawLocalMoon(
                state = state,
                skyToScreen = ::skyToScreen
            )

            // 6. Draw Compass Direction Labels along the Horizon
            drawCompassLabels(
                centerX = centerX,
                centerY = centerY,
                skyRadius = skyRadius,
                lookAz = lookAz
            )
        }

        // Top-left Observer info overlay
        Surface(
            color = Color(0x770F172A),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = "ВИД С ТВЕРДИ • ${state.observerLocation.nameRu}",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = "Высота Солнца: ${String.format("%.1f°", state.telemetry.sunApparentAltitudeDeg)} • Азимут: ${String.format("%.1f°", state.telemetry.sunApparentAzimuthDeg)}",
                    color = Color(0xFFFDE047),
                    fontSize = 11.sp
                )
                Text(
                    text = "Высота Луны: ${String.format("%.1f°", state.telemetry.moonApparentAltitudeDeg)} • Фаза: ${state.telemetry.moonPhaseNameRu}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp
                )
            }
        }

        // Quick Compass Look Direction Buttons at bottom of view
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickLookChip(label = "Север (0°)", active = state.observerCamera.azimuthHeadingDeg in 345f..360f || state.observerCamera.azimuthHeadingDeg in 0f..15f) {
                onSetCamera(0f, 45f)
            }
            QuickLookChip(label = "Восток (90°)", active = state.observerCamera.azimuthHeadingDeg in 75f..105f) {
                onSetCamera(90f, 45f)
            }
            QuickLookChip(label = "Юг (180°)", active = state.observerCamera.azimuthHeadingDeg in 165f..195f) {
                onSetCamera(180f, 45f)
            }
            QuickLookChip(label = "Запад (270°)", active = state.observerCamera.azimuthHeadingDeg in 255f..285f) {
                onSetCamera(270f, 45f)
            }
            QuickLookChip(label = "Солнце ☀️", active = false) {
                onSetCamera(state.telemetry.sunApparentAzimuthDeg.toFloat(), state.telemetry.sunApparentAltitudeDeg.toFloat().coerceAtLeast(15f))
            }
        }
    }
}

@Composable
private fun QuickLookChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = active,
        onClick = onClick,
        label = { Text(label, fontSize = 10.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF0284C7),
            selectedLabelColor = Color.White,
            containerColor = Color(0x601E293B),
            labelColor = Color(0xFFCBD5E1)
        )
    )
}

private fun DrawScope.drawElevationGrid(
    centerX: Float,
    centerY: Float,
    skyRadius: Float,
    lookAz: Float,
    lookEl: Float
) {
    val lookElRad = Math.toRadians(lookEl.toDouble())

    // On a tilted view the horizon is an ellipse, not a fixed centered circle.
    drawOval(
        color = Color(0x6038BDF8),
        topLeft = Offset(
            centerX - skyRadius,
            centerY + (sin(lookElRad) * skyRadius).toFloat() - (cos(lookElRad) * skyRadius).toFloat()
        ),
        bottomRight = Offset(
            centerX + skyRadius,
            centerY + (sin(lookElRad) * skyRadius).toFloat() + (cos(lookElRad) * skyRadius).toFloat()
        ),
        style = Stroke(width = 2.0f)
    )

    // Elevation rings: 30°, 60°
    val rings = listOf(
        Pair(30.0, "30°"),
        Pair(60.0, "60°")
    )
    for ((alt, label) in rings) {
        val altitudeRad = Math.toRadians(alt)
        val horizontalRadius = (cos(altitudeRad) * skyRadius).toFloat()
        val verticalRadius = (cos(altitudeRad) * sin(lookElRad) * skyRadius).toFloat()
        val ringCenterY = centerY + (sin(altitudeRad) * cos(lookElRad) * skyRadius).toFloat()
        drawOval(
            color = Color(0x2538BDF8),
            topLeft = Offset(centerX - horizontalRadius, ringCenterY - verticalRadius),
            bottomRight = Offset(centerX + horizontalRadius, ringCenterY + verticalRadius),
            style = Stroke(width = 1.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        )
    }

    // Zenith marker (90° Altitude)
    drawCircle(
        color = Color(0x8038BDF8),
        radius = 4f,
        center = Offset(centerX, centerY)
    )

    // Azimuth radial spokes (every 45 degrees)
    for (a in 0 until 8) {
        val angleDeg = a * 45.0 - lookAz
        val rad = Math.toRadians(angleDeg)
        val endX = centerX + skyRadius * sin(rad).toFloat()
        val endY = centerY + (skyRadius * sin(lookElRad) * cos(rad)).toFloat()

        drawLine(
            color = Color(0x2038BDF8),
            start = Offset(centerX, centerY),
            end = Offset(endX, endY),
            strokeWidth = 1.0f
        )
    }
}

private fun DrawScope.drawCompassLabels(
    centerX: Float,
    centerY: Float,
    skyRadius: Float,
    lookAz: Float
) {
    val compassPoints = listOf(
        Pair(0.0, "С (N)"),
        Pair(45.0, "СВ"),
        Pair(90.0, "В (E)"),
        Pair(135.0, "ЮВ"),
        Pair(180.0, "Ю (S)"),
        Pair(225.0, "ЮЗ"),
        Pair(270.0, "З (W)"),
        Pair(315.0, "СЗ")
    )

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(200, 148, 163, 184)
        textSize = 28f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    for ((az, label) in compassPoints) {
        val relAz = az - lookAz
        val rad = Math.toRadians(relAz)
        val labelRadius = skyRadius + 32f
        val lx = centerX + labelRadius * sin(rad).toFloat()
        val ly = centerY - labelRadius * cos(rad).toFloat() + 10f

        drawContext.canvas.nativeCanvas.drawText(label, lx, ly, paint)
    }
}

private fun DrawScope.drawLocalStars(
    state: FlatEarthAppState,
    skyToScreen: (Double, Double) -> Offset?
) {
    val starPositions = mutableMapOf<String, Offset>()

    for (star in MAJOR_STARS) {
        val (altDeg, azDeg) = CelestialEngine.equatorialToHorizontal(
            raHours = star.rightAscensionHours,
            decDeg = star.declinationDeg,
            obsLatDeg = state.observerLocation.latitude,
            obsLonDeg = state.observerLocation.longitude,
            timestampMillis = state.currentTimestampMillis
        )

        val pt = skyToScreen(azDeg, altDeg)
        if (pt != null) {
            starPositions[star.name] = pt
            val radius = (3.5f - star.magnitude * 0.6f).coerceIn(1.5f, 5.0f)
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = radius,
                center = pt
            )
        }
    }

    // Connect constellations visible in observer's sky
    for (line in CONSTELLATION_LINES) {
        val p1 = starPositions[line.star1]
        val p2 = starPositions[line.star2]
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

private fun DrawScope.drawLocalSun(
    state: FlatEarthAppState,
    skyToScreen: (Double, Double) -> Offset?
) {
    val sunAlt = state.telemetry.sunApparentAltitudeDeg
    val sunAz = state.telemetry.sunApparentAzimuthDeg

    val pt = skyToScreen(sunAz, sunAlt) ?: return

    // Solar glare halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x80FDE047),
                Color(0x30F59E0B),
                Color(0x00D97706)
            ),
            center = pt,
            radius = 60f
        ),
        radius = 60f,
        center = pt
    )

    // Sun disk
    val radiusPx = (state.telemetry.sunApparentDiameterArcmin / 32.0 * 14.0).toFloat().coerceIn(8f, 22f)
    drawCircle(
        color = SunGold,
        radius = radiusPx,
        center = pt
    )

    // Sub-solar indicator label
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(240, 254, 240, 138)
        textSize = 24f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
        "Солнце (${String.format("%.1f°", sunAlt)})",
        pt.x,
        pt.y - radiusPx - 10f,
        paint
    )
}

private fun DrawScope.drawLocalMoon(
    state: FlatEarthAppState,
    skyToScreen: (Double, Double) -> Offset?
) {
    val moonAlt = state.telemetry.moonApparentAltitudeDeg
    val moonAz = state.telemetry.moonApparentAzimuthDeg

    val pt = skyToScreen(moonAz, moonAlt) ?: return

    // Moon soft halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x40E2E8F0),
                Color(0x1094A3B8),
                Color(0x000F172A)
            ),
            center = pt,
            radius = 35f
        ),
        radius = 35f,
        center = pt
    )

    // Moon disk
    val radiusPx = 10f
    drawCircle(
        color = Color(0xFF1E293B),
        radius = radiusPx,
        center = pt
    )

    // Phase crescent
    val phaseFrac = state.telemetry.moonPhaseFraction
    if (phaseFrac > 0.05f) {
        drawCircle(
            color = MoonSilver,
            radius = radiusPx * phaseFrac.coerceIn(0.25f, 1.0f),
            center = pt
        )
    }

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(220, 226, 232, 240)
        textSize = 22f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
        "Луна (${String.format("%.1f°", moonAlt)})",
        pt.x,
        pt.y + radiusPx + 24f,
        paint
    )
}
