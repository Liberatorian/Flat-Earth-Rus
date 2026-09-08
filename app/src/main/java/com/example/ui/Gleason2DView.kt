package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.model.CityLocation
import com.example.model.FlatEarthAppState
import com.example.model.FlatEarthConstants
import com.example.model.GleasonMapData
import com.example.model.PRESET_CITIES
import com.example.ui.theme.EquatorGold
import com.example.ui.theme.GridCyan
import com.example.ui.theme.IceBlue
import com.example.ui.theme.IceWallCyan
import com.example.ui.theme.MoonSilver
import com.example.ui.theme.SunGold
import com.example.ui.theme.TropicRed
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Gleason 2D Map: Standard Azimuthal Equidistant projection of the Flat Earth.
 * Features 24-hour solar dial, concentric tropics, day/night light cone,
 * real-time positions of Sun & Moon, and tap-to-set observer.
 */
@Composable
fun Gleason2DView(
    state: FlatEarthAppState,
    onTapLocation: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    // Calculate tapped lat/lon from click offset
                    // Center and radius will be computed in canvas; approximate from box size
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val width = size.width
                        val height = size.height
                        val centerX = width / 2f
                        val centerY = height * 0.52f
                        val discRadius = minOf(width, height) * 0.42f

                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        val dist = sqrt(dx * dx + dy * dy)

                        if (dist <= discRadius * 1.05f) {
                            val rNorm = (dist / discRadius).coerceIn(0f, 1f)
                            val lat = 90.0 - (rNorm * 180.0)
                            var lon = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble()))
                            if (lon < -180) lon += 360
                            if (lon > 180) lon -= 360
                            onTapLocation(lat, lon)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.52f
            val discRadius = minOf(width, height) * 0.42f

            fun geoToPixel(latitude: Double, longitude: Double): Offset {
                val (xKm, yKm) = com.example.model.CelestialEngine.geoToDiscKm(latitude, longitude, state.projection)
                return Offset(
                    centerX + (xKm / FlatEarthConstants.DISC_RADIUS_KM * discRadius).toFloat(),
                    centerY + (yKm / FlatEarthConstants.DISC_RADIUS_KM * discRadius).toFloat()
                )
            }

            // 1. Draw Space & Polar Border Background
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF060B18), Color(0xFF030712)),
                    center = Offset(centerX, centerY),
                    radius = discRadius * 1.5f
                )
            )

            // 2. Draw Ocean Disc Base
            drawCircle(
                color = Color(0xFF091428),
                radius = discRadius,
                center = Offset(centerX, centerY)
            )

            // 3. Draw The Great Antarctic Ice Wall Rim
            val iceWallThickness = discRadius * 0.08f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3067E8F9), Color(0xFF38BDF8), Color(0xFFE0F2FE)),
                    center = Offset(centerX, centerY),
                    radius = discRadius
                ),
                radius = discRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = iceWallThickness)
            )

            // 4. Draw Continents on the Flat Disc
            for (continent in GleasonMapData.ALL_CONTINENTS) {
                val path = GleasonMapData.polygonToPath(continent, centerX, centerY, discRadius, state.projection)
                drawPath(path = path, color = Color(0xFF1E3A2F))
                drawPath(path = path, color = Color(0xFF34D399), style = Stroke(width = 1.4f))
            }

            // 5. Draw Concentric Parallels (Tropics, Equator, Circles)
            if (state.layers.showTropicsAndEquator) {
                drawCircle(
                    color = IceBlue.copy(alpha = 0.5f),
                    radius = discRadius * (FlatEarthConstants.ARCTIC_CIRCLE_KM / FlatEarthConstants.DISC_RADIUS_KM).toFloat(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                )
                drawCircle(
                    color = TropicRed,
                    radius = discRadius * (FlatEarthConstants.TROPIC_CANCER_KM / FlatEarthConstants.DISC_RADIUS_KM).toFloat(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                )
                drawCircle(
                    color = EquatorGold,
                    radius = discRadius * (FlatEarthConstants.EQUATOR_RADIUS_KM / FlatEarthConstants.DISC_RADIUS_KM).toFloat(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.2f)
                )
                drawCircle(
                    color = TropicRed,
                    radius = discRadius * (FlatEarthConstants.TROPIC_CAPRICORN_KM / FlatEarthConstants.DISC_RADIUS_KM).toFloat(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                )
                drawCircle(
                    color = IceWallCyan.copy(alpha = 0.6f),
                    radius = discRadius * (FlatEarthConstants.ANTARCTIC_CIRCLE_KM / FlatEarthConstants.DISC_RADIUS_KM).toFloat(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                )
            }

            // 6. Draw 24 Meridians (15 degrees each = 1 hour time zone)
            if (state.layers.showCoordinateGrid) {
                for (h in 0 until 24) {
                    val angleRad = h * (2.0 * PI / 24.0)
                    val endX = centerX + discRadius * sin(angleRad).toFloat()
                    val endY = centerY - discRadius * cos(angleRad).toFloat()
                    val isPrime = (h == 0 || h == 12)
                    drawLine(
                        color = if (isPrime) GridCyan.copy(alpha = 0.6f) else GridCyan.copy(alpha = 0.2f),
                        start = Offset(centerX, centerY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isPrime) 1.5f else 0.8f
                    )
                }
            }

            // 7. Draw Day / Night Spotlight Cone & Penumbra
            if (state.layers.showDayNightCone) {
                val sunPx = geoToPixel(state.telemetry.sunLatitude, state.telemetry.sunLongitude)
                val spotRadiusPx = (FlatEarthConstants.SUN_SPOTLIGHT_RADIUS_KM / FlatEarthConstants.DISC_RADIUS_KM * discRadius).toFloat()

                // Daylight pool
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FDE047),
                            Color(0x35F59E0B),
                            Color(0x10EA580C),
                            Color(0x000F172A)
                        ),
                        center = sunPx,
                        radius = spotRadiusPx * 1.15f
                    ),
                    radius = spotRadiusPx * 1.15f,
                    center = sunPx
                )

                // Spotlight terminator boundary line
                drawCircle(
                    color = Color(0x70FDE047),
                    radius = spotRadiusPx,
                    center = sunPx,
                    style = Stroke(width = 2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )
            }

            // 8. Draw City Markers
            if (state.layers.showCityMarkers) {
                for (city in PRESET_CITIES) {
                    val cityPoint = geoToPixel(city.latitude, city.longitude)
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.75f),
                        radius = 3.5f,
                        center = cityPoint
                    )
                }
            }

            // 9. Draw Observer Marker
            val obsPx = geoToPixel(state.observerLocation.latitude, state.observerLocation.longitude)
            val pulseR = 12f + 4f * sin((System.currentTimeMillis() % 2000) / 2000.0 * 2 * PI).toFloat()
            drawCircle(color = Color(0x6006B6D4), radius = pulseR, center = obsPx)
            drawCircle(color = Color(0xFF06B6D4), radius = 6f, center = obsPx)
            drawCircle(color = Color.White, radius = 2.5f, center = obsPx)

            // Observer label
            val obsPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(240, 6, 182, 212)
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                state.observerLocation.nameRu,
                obsPx.x,
                obsPx.y - 14f,
                obsPaint
            )

            // 10. Draw Sun
            val sunPos = geoToPixel(state.telemetry.sunLatitude, state.telemetry.sunLongitude)
            // Sun orbit ring (seasonal circular path)
            val sunOrbitRadiusPx = (state.telemetry.sunDiscRadiusKm / FlatEarthConstants.DISC_RADIUS_KM * discRadius).toFloat()
            drawCircle(
                color = Color(0x40FDE047),
                radius = sunOrbitRadiusPx,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )

            // Sun body & glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE58), Color(0x90FFB300), Color(0x00FF8F00)),
                    center = sunPos,
                    radius = 24f
                ),
                radius = 24f,
                center = sunPos
            )
            drawCircle(color = SunGold, radius = 9f, center = sunPos)

            // 11. Draw Moon
            val moonPos = geoToPixel(state.telemetry.moonLatitude, state.telemetry.moonLongitude)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x90F1F5F9), Color(0x3094A3B8), Color(0x000F172A)),
                    center = moonPos,
                    radius = 18f
                ),
                radius = 18f,
                center = moonPos
            )
            drawCircle(color = Color(0xFF1E293B), radius = 7.5f, center = moonPos)
            if (state.telemetry.moonPhaseFraction > 0.05f) {
                drawCircle(color = MoonSilver, radius = 7.5f * state.telemetry.moonPhaseFraction.coerceIn(0.2f, 1.0f), center = moonPos)
            }

            // 12. Draw 24-Hour Solar Clock around Rim
            val clockPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(190, 203, 213, 225)
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            for (h in 0 until 24) {
                val angleRad = h * (2.0 * PI / 24.0)
                val rText = discRadius + 22f
                val tx = centerX + rText * sin(angleRad).toFloat()
                val ty = centerY - rText * cos(angleRad).toFloat() + 8f
                val hourLabel = String.format("%02d:00", (12 + h) % 24)
                if (h % 3 == 0) {
                    drawContext.canvas.nativeCanvas.drawText(hourLabel, tx, ty, clockPaint)
                }
            }
        }

        // Top info card
        Surface(
            color = Color(0x770F172A),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "КАРТА ГЛИСОНА (2D) • Координатная сетка • Солнечный циферблат 24ч\n(Нажмите в любую точку карты для переноса наблюдателя)",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
