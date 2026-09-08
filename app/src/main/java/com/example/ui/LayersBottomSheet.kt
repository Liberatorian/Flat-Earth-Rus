package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LayerSettings
import com.example.model.MapProjection
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayersBottomSheet(
    layers: LayerSettings,
    projection: MapProjection,
    onToggleLayer: (LayerSettings.() -> LayerSettings) -> Unit,
    onProjectionChange: (MapProjection) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D1730),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Слои и Отображение",
                        color = Color(0xFFF8FAFC),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Проекция координатной сетки",
                color = Color(0xFFF8FAFC),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Меняет преобразование latitude/longitude → x/y",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProjectionChip("AE", MapProjection.GLEASON_AE, projection, onProjectionChange)
                ProjectionChip("Stereo", MapProjection.STEREOGRAPHIC, projection, onProjectionChange)
                ProjectionChip("Ortho", MapProjection.ORTHOGRAPHIC, projection, onProjectionChange)
            }

            Spacer(modifier = Modifier.height(10.dp))

            LayerToggleRow(
                label = "Координатная сетка диска (Меридианы 15°)",
                description = "Полярные градусные деления и часовые пояса",
                checked = layers.showCoordinateGrid,
                onCheckedChange = { onToggleLayer { copy(showCoordinateGrid = it) } }
            )

            LayerToggleRow(
                label = "Тропики и Экватор",
                description = "Тропик Рака (север), Экватор, Тропик Козерога (юг)",
                checked = layers.showTropicsAndEquator,
                onCheckedChange = { onToggleLayer { copy(showTropicsAndEquator = it) } }
            )

            LayerToggleRow(
                label = "Конус дня и ночи (Spotlight)",
                description = "Пятно освещения Солнца и сумеречная пенумбра",
                checked = layers.showDayNightCone,
                onCheckedChange = { onToggleLayer { copy(showDayNightCone = it) } }
            )

            LayerToggleRow(
                label = "Световой луч Солнца",
                description = "Объемный луч от Солнца к тверди земли",
                checked = layers.showSunBeam,
                onCheckedChange = { onToggleLayer { copy(showSunBeam = it) } }
            )

            LayerToggleRow(
                label = "Звездный купол и созвездия",
                description = "Полярная звезда (Polaris), Большая Медведица, Орион",
                checked = layers.showStarsAndConstellations,
                onCheckedChange = { onToggleLayer { copy(showStarsAndConstellations = it) } }
            )

            LayerToggleRow(
                label = "Маркеры городов мира",
                description = "Москва, Лондон, Нью-Йорк, Токио, Сидней, Каир и др.",
                checked = layers.showCityMarkers,
                onCheckedChange = { onToggleLayer { copy(showCityMarkers = it) } }
            )

            LayerToggleRow(
                label = "Хрустальный купол (Фирмамент)",
                description = "Прозрачные арки небесного свода",
                checked = layers.showFirmamentGlow,
                onCheckedChange = { onToggleLayer { copy(showFirmamentGlow = it) } }
            )

            LayerToggleRow(
                label = "Оптический путь наблюдения",
                description = "Геометрический луч, рефракция и потеря контраста",
                checked = layers.showOpticalRays,
                onCheckedChange = { onToggleLayer { copy(showOpticalRays = it) } }
            )

            LayerToggleRow(
                label = "Показывать допущения модели",
                description = "Отмечает проекцию, масштабирование и нестандартную оптику",
                checked = layers.showScientificAssumptions,
                onCheckedChange = { onToggleLayer { copy(showScientificAssumptions = it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProjectionChip(
    label: String,
    value: MapProjection,
    selected: MapProjection,
    onSelect: (MapProjection) -> Unit
) {
    FilterChip(
        selected = value == selected,
        onClick = { onSelect(value) },
        label = { Text(label, fontSize = 10.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF0284C7),
            selectedLabelColor = Color.White,
            containerColor = Color(0x401E293B),
            labelColor = Color(0xFFCBD5E1)
        )
    )
}

@Composable
private fun LayerToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color(0xFFF8FAFC),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
