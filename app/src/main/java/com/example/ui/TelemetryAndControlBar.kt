package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlatEarthAppState

@Composable
fun TelemetryAndControlBar(
    state: FlatEarthAppState,
    onTogglePlayPause: () -> Unit,
    onRealTimeSync: (Boolean) -> Unit,
    onSetSpeed: (Double) -> Unit,
    onJumpSeason: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xF2070D1F),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Row 1: Time, Play/Pause, Real-Time Sync, and Live Day/Night Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .background(if (state.isPlaying) Color(0xFF0284C7) else Color(0xFF334155), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Пауза" else "Старт",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = state.telemetry.localHourString,
                            color = Color(0xFFF8FAFC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.telemetry.seasonNameRu,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                }

                // Real-Time toggle chip
                FilterChip(
                    selected = state.isRealTime,
                    onClick = { onRealTimeSync(!state.isRealTime) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Реал. время", fontSize = 11.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0x401E293B),
                        labelColor = Color(0xFF94A3B8)
                    )
                )

                // Day / Night state badge for observer
                Surface(
                    color = if (state.telemetry.isObserverDaytime) Color(0x35FDE047) else Color(0x3538BDF8),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.telemetry.isObserverDaytime) "☀️ День" else "🌙 Ночь",
                            color = if (state.telemetry.isObserverDaytime) Color(0xFFFDE047) else Color(0xFFBAE6FD),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Speed Multipliers (Horizontal Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Скорость:", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                SpeedChip("1x", 1.0, state.timeSpeedMultiplier, state.isRealTime) { onSetSpeed(1.0) }
                SpeedChip("60x (1м/с)", 60.0, state.timeSpeedMultiplier, state.isRealTime) { onSetSpeed(60.0) }
                SpeedChip("600x (10м/с)", 600.0, state.timeSpeedMultiplier, state.isRealTime) { onSetSpeed(600.0) }
                SpeedChip("3600x (1ч/с)", 3600.0, state.timeSpeedMultiplier, state.isRealTime) { onSetSpeed(3600.0) }
                SpeedChip("86400x (1д/с)", 86400.0, state.timeSpeedMultiplier, state.isRealTime) { onSetSpeed(86400.0) }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 3: Seasonal Quick Jumpers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Сезоны:", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                SeasonChip("Весна (Рак)", 80) { onJumpSeason(80) }
                SeasonChip("Лето (Сев)", 172) { onJumpSeason(172) }
                SeasonChip("Осень (Экв)", 264) { onJumpSeason(264) }
                SeasonChip("Зима (Юг)", 355) { onJumpSeason(355) }
            }
        }
    }
}

@Composable
private fun SpeedChip(
    label: String,
    speed: Double,
    currentSpeed: Double,
    isRealTime: Boolean,
    onClick: () -> Unit
) {
    val isSelected = (!isRealTime && kotlin.math.abs(currentSpeed - speed) < 0.1)
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 10.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF0284C7),
            selectedLabelColor = Color.White,
            containerColor = Color(0x351E293B),
            labelColor = Color(0xFF94A3B8)
        ),
        modifier = Modifier.height(28.dp)
    )
}

@Composable
private fun SeasonChip(
    label: String,
    day: Int,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label, fontSize = 10.sp) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0x250284C7),
            labelColor = Color(0xFF7DD3FC)
        ),
        modifier = Modifier.height(26.dp)
    )
}
