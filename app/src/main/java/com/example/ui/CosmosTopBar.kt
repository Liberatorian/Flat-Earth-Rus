package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlatEarthAppState
import com.example.model.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmosTopBar(
    state: FlatEarthAppState,
    onViewModeChange: (ViewMode) -> Unit,
    onOpenLocationPicker: () -> Unit,
    onOpenLayers: () -> Unit,
    onOpenTheory: () -> Unit,
    onOpenReference: () -> Unit,
    onToggleAmbientAudio: () -> Unit,
    onAmbientVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xF2070D1F),
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Row 1: App Title + Observer Location Button + Theory Button + Layers Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FLAT EARTH COSMOS",
                        color = Color(0xFFF8FAFC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Observer Location Chip
                    Surface(
                        onClick = onOpenLocationPicker,
                        color = Color(0xFF132042),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = state.observerLocation.nameRu,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Layers Button
                    IconButton(
                        onClick = onOpenLayers,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Слои",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Theory & Physics Formulas Button
                    IconButton(
                        onClick = onOpenTheory,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = "Формулы и физика",
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenReference,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Справочник модели",
                            tint = Color(0xFF67E8F9),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    var audioMenuExpanded by remember { mutableStateOf(false) }
                    androidx.compose.foundation.layout.Box {
                        IconButton(
                            onClick = { audioMenuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = if (state.isAmbientAudioEnabled) "♫" else "♪",
                                color = if (state.isAmbientAudioEnabled) Color(0xFF34D399) else Color(0xFF64748B),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        DropdownMenu(
                            expanded = audioMenuExpanded,
                            onDismissRequest = { audioMenuExpanded = false }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Фоновый звук", modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = state.isAmbientAudioEnabled,
                                        onCheckedChange = { onToggleAmbientAudio() }
                                    )
                                }
                                Text("Громкость ${(state.ambientAudioVolume * 100).toInt()}%", fontSize = 12.sp)
                                Slider(
                                    value = state.ambientAudioVolume,
                                    onValueChange = onAmbientVolumeChange,
                                    modifier = Modifier.width(190.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: View Mode Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ViewModeChip(
                    label = "🌐 3D Купол",
                    isSelected = state.viewMode == ViewMode.DOME_3D,
                    onClick = { onViewModeChange(ViewMode.DOME_3D) },
                    modifier = Modifier.weight(1f)
                )

                ViewModeChip(
                    label = "👁️ Вид с Земли",
                    isSelected = state.viewMode == ViewMode.OBSERVER_SKY,
                    onClick = { onViewModeChange(ViewMode.OBSERVER_SKY) },
                    modifier = Modifier.weight(1f)
                )

                ViewModeChip(
                    label = "🗺️ Карта 2D",
                    isSelected = state.viewMode == ViewMode.GLEASON_2D,
                    onClick = { onViewModeChange(ViewMode.GLEASON_2D) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ViewModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF132042),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(30.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
