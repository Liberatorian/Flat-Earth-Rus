package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlatEarthAppState
import kotlin.math.cos
import kotlin.math.sin

/**
 * Exterior educational view for the scientific model.
 * Distances are logarithmically compressed so the inner system remains visible.
 */
@Composable
fun ScientificSystemView(
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
                    onCameraDelta(-dragAmount.y * 0.2f, dragAmount.x * 0.25f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.53f)
            val scale = minOf(size.width, size.height) * 0.25f * state.camera3D.zoom
            val yaw = Math.toRadians(state.camera3D.yawDeg.toDouble())
            val pitch = Math.toRadians(state.camera3D.pitchDeg.toDouble())
            val flattened = cos(pitch).toFloat().coerceIn(0.18f, 1f)

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF111C3A), Color(0xFF030712)),
                    center = center,
                    radius = size.maxDimension * 0.75f
                )
            )

            fun project(radiusAu: Double, phaseDeg: Double): Offset {
                val angle = Math.toRadians(phaseDeg) + yaw
                val x = radiusAu * cos(angle)
                val y = radiusAu * sin(angle)
                return Offset(
                    center.x + (x * scale).toFloat(),
                    center.y - (y * scale * flattened).toFloat()
                )
            }

            val orbitRadii = listOf(0.39, 0.72, 1.0, 1.52, 2.2, 3.2, 4.1, 5.2)
            orbitRadii.forEach { radius ->
                drawCircle(
                    color = Color(0x5538BDF8),
                    radius = (radius * scale).toFloat(),
                    center = center,
                    style = Stroke(width = 1f)
                )
            }

            val days = (state.currentTimestampMillis / 86_400_000.0) - 10_957.5
            val earthPhase = days / 365.256 * 360.0
            val earth = project(1.0, earthPhase)
            val moonPhase = days / 27.321661 * 360.0
            val moon = Offset(
                earth.x + (cos(Math.toRadians(moonPhase)) * scale * 0.08).toFloat(),
                earth.y - (sin(Math.toRadians(moonPhase)) * scale * 0.08 * flattened).toFloat()
            )

            drawCircle(Color(0xFFFFC857), radius = scale * 0.075f, center = center)
            drawCircle(Color(0xFF2563EB), radius = scale * 0.028f, center = earth)
            drawCircle(Color(0xFFD1D5DB), radius = scale * 0.010f, center = moon)
            drawLine(
                color = Color(0x8894A3B8),
                start = earth,
                end = moon,
                strokeWidth = 1f,
                cap = StrokeCap.Round
            )

            drawLabel("СОЛНЦЕ", center + Offset(10f, -scale * 0.08f), Color(0xFFFFE08A))
            drawLabel("Земля", earth + Offset(8f, -8f), Color(0xFF93C5FD))
            drawLabel("Луна", moon + Offset(8f, 12f), Color(0xFFE2E8F0))
        }

        Surface(
            color = Color(0xAA0F172A),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "СОЛНЕЧНАЯ СИСТЕМА • научная схема\n" +
                    "Орбиты сжаты логарифмически • ${state.observerLocation.nameRu}\n" +
                    "Положение Земли: ${String.format("%.1f°", (state.currentTimestampMillis / 86_400_000.0) % 360.0)}",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLabel(
    text: String,
    position: Offset,
    color: Color
) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        position.x,
        position.y,
        android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = 26f
            isAntiAlias = true
        }
    )
}
