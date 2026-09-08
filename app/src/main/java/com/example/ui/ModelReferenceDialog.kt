package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModelReferenceDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1730),
        title = {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.foundation.layout.Row {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF67E8F9))
                    Text("Справочник исследователя", color = Color(0xFFF8FAFC), modifier = Modifier.padding(start = 8.dp))
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Закрыть", tint = Color(0xFF94A3B8)) }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { ReferenceBlock("МАСШТАБ", "Диск R = 20 000 км; экватор = 10 000 км; физическая ледяная стена = 75 м; купол = 0–10 500 км; Солнце = 4 800 км; Луна = 4 750 км.") }
                item { ReferenceBlock("КООРДИНАТЫ", "r = R · (90° − latitude) / 180°\nx = r · sin(longitude)\ny = −r · cos(longitude)\nПроекции AE, stereographic и orthographic можно сравнивать отдельно.") }
                item { ReferenceBlock("СОЛНЦЕ И ЛУНА", "Солнечная широта: δ(t) = 23.44° · sin(2π(t − 80) / 365.25).\nЛунная фаза: k = (1 − cos(Δφ)) / 2; синодический цикл = 29.530589 суток.") }
                item { ReferenceBlock("НАБЛЮДАТЕЛЬ", "Для каждого светила доступны геометрическая высота, наблюдаемая высота после атмосферного пути, азимут, расстояние и угловой диаметр.") }
                item { ReferenceBlock("ПРОВЕРЯЕМЫЕ СЛЕДСТВИЯ", "Сравнивайте положение Солнца в разных широтах, размер светила с расстоянием, вращение звёздного купола, границу пятна света и прохождение луча через атмосферу.") }
                item { ReferenceBlock("ОГРАНИЧЕНИЯ", "Модель является экспериментальной визуализацией альтернативной геометрии. Параметры купола, локального освещения и рефракции явно задаются и не являются утверждением о физической картине мира.") }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun ReferenceBlock(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF132042))) {
        Column(modifier = Modifier.padding(11.dp)) {
            Text(title, color = Color(0xFF67E8F9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(body, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}