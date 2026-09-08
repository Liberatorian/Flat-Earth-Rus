package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CelestialTelemetry

@Composable
fun TelemetryDetailCard(
    telemetry: CelestialTelemetry,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xD00D1730)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Телеметрия светил и наблюдателя",
                        color = Color(0xFFF8FAFC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Свернуть" else "Подробнее",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Quick summary row when collapsed
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Солнце: h=${String.format("%.1f°", telemetry.sunApparentAltitudeDeg)} Az=${String.format("%.0f°", telemetry.sunApparentAzimuthDeg)}",
                        color = Color(0xFFFDE047),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Скорость: ${telemetry.sunSpeedKmH.toInt()} км/ч",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Луна: ${telemetry.moonPhaseNameRu}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp
                    )
                }
            }

            // Expanded in-depth mathematical telemetry
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Grid of telemetry values
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryColumn(
                            title = "СОЛНЦЕ (SOL)",
                            color = Color(0xFFFDE047),
                            modifier = Modifier.weight(1f),
                            items = listOf(
                                "Подсолнечная шир." to String.format("%.2f°", telemetry.sunLatitude),
                                "Подсолнечная долг." to String.format("%.2f°", telemetry.sunLongitude),
                                "Радиус от полюса" to "${telemetry.sunDiscRadiusKm.toInt()} км",
                                "Орбитальная скорость" to "${telemetry.sunSpeedKmH.toInt()} км/ч",
                                "Высота над твердью" to "${telemetry.sunAltitudeKm.toInt()} км",
                                "Видимый диаметр" to String.format("%.1f'", telemetry.sunApparentDiameterArcmin)
                            )
                        )

                        TelemetryColumn(
                            title = "ЛУНА (LUNA)",
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.weight(1f),
                            items = listOf(
                                "Фаза" to telemetry.moonPhaseNameRu,
                                "Освещенность" to "${(telemetry.moonPhaseFraction * 100).toInt()}%",
                                "Возраст Луны" to String.format("%.1f дн.", telemetry.moonAgeDays),
                                "Подлунная шир." to String.format("%.2f°", telemetry.moonLatitude),
                                "Подлунная долг." to String.format("%.2f°", telemetry.moonLongitude),
                                "Высота светила" to "${telemetry.moonAltitudeKm.toInt()} км"
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Observer Section
                    Surface(
                        color = Color(0xFF070E22),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "НАБЛЮДАТЕЛЬ: ${telemetry.observerCityName} (${String.format("%.2f°N, %.2f°E", telemetry.observerLat, telemetry.observerLon)})",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Дист. до Солнца: ${telemetry.observerDistanceToSunKm.toInt()} км",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Дист. до Луны: ${telemetry.observerDistanceToMoonKm.toInt()} км",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Угол Солнца: h=${String.format("%.1f°", telemetry.sunApparentAltitudeDeg)}, Az=${String.format("%.1f°", telemetry.sunApparentAzimuthDeg)}",
                                    color = Color(0xFFFDE047),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Угол Луны: h=${String.format("%.1f°", telemetry.moonApparentAltitudeDeg)}, Az=${String.format("%.1f°", telemetry.moonApparentAzimuthDeg)}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "ОПТИКА: геом. Солнце h=${String.format("%.2f°", telemetry.sunGeometricAltitudeDeg)} → наблюд. h=${String.format("%.2f°", telemetry.sunApparentAltitudeDeg)}",
                                color = Color(0xFF67E8F9),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryColumn(
    title: String,
    color: Color,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF070E22),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            for ((key, value) in items) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = key, color = Color(0xFF64748B), fontSize = 9.sp)
                    Text(text = value, color = Color(0xFFE2E8F0), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
