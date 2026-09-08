package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TheoryDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1730),
        titleContentColor = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = null,
                        tint = Color(0xFFFDE047)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Небесная Механика и Физика",
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
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FormulaSection(
                        title = "1. Геометрия диска и проекция Глисона (AE)",
                        content = "Модель базируется на полярной азимутальной эквидистантной проекции. Северный магнитный полюс находится в центре координат (0,0). Вся суша окружена внешним ледяным барьером Антарктиды.",
                        formula = "r = R_{диск} · ((90° - φ) / 180°)\n" +
                                "x = r · sin(λ),   y = -r · cos(λ)\n" +
                                "R_{экватор} = 10 000 км, R_{лед} = 20 000 км"
                    )
                }

                item {
                    FormulaSection(
                        title = "2. Спиральная кинематика Солнца",
                        content = "Солнце представляет собой локальный сферический излучатель диаметром ~52 км, парящий на высоте H ≈ 5000 км. Оно совершает оборот за 24 часа (15°/час) и спиралевидно мигрирует между тропиками в течение года:",
                        formula = "δ(t) = 23.44° · sin(2π(t - 80) / 365.25)\n" +
                                "r_{солн} = R_{экв} · (1 - δ / 90°)\n" +
                                "v(t) = 2π · r_{солн} / 24ч\n" +
                                "v_{июнь} ≈ 1937 км/ч (Север), v_{дек} ≈ 3298 км/ч (Юг)"
                    )
                }

                item {
                    FormulaSection(
                        title = "3. Оптика дня и ночи (Конус спотлайта)",
                        content = "Вместо геометрической тени сферы, смена дня и ночи объясняется локальным конусом направленного излучения Солнца (spotlight) с эффективным радиусом освещения R ≈ 9600 км. За пределами конуса наступает естественная ночь с плавным сумеречным переходом (пенумброй).",
                        formula = "d_{планар} = √((x_{солн} - x_{набл})² + (y_{солн} - y_{набл})²)\n" +
                                "День: d_{планар} ≤ R_{свет}\n" +
                                "Сумерки: R_{свет} < d_{планар} ≤ R_{свет} + 1800 км"
                    )
                }

                item {
                    FormulaSection(
                        title = "4. Закон оптической перспективы Роуботэма",
                        content = "Светило 'заходит за горизонт' не из-за искривления земной поверхности, а по закону угловой перспективы и предела человеческого зрения (точка схода / vanishing point) в сочетании с оптической плотностью приземного слоя атмосферы:",
                        formula = "α = arctan(H / d_{планар}) · (180° / π)\n" +
                                "D_{видимый} = 2 · arctan(D_{ист} / 2D_{луч})\n" +
                                "При d > 11 000 км: угол α < 1.0° (слияние с горизонтом)"
                    )
                }

                item {
                    FormulaSection(
                        title = "5. Орбитальная динамика и фазы Луны",
                        content = "Луна обращается на высоте H ≈ 4950 км со скоростью чуть меньшей, чем у Солнца (синодический месяц = 29.53 суток, суточное отставание ≈ 50 минут). Фаза зависит от относительного углового расстояния между Солнцем и Луной:",
                        formula = "Δφ = θ_{солн} - θ_{луны}  (mod 360°)\n" +
                                "Освещенность k = (1 - cos(Δφ)) / 2\n" +
                                "Новолуние: 0° (0%) • Первая четв.: 90° (50%) • Полнолуние: 180° (100%)"
                    )
                }

                item {
                    FormulaSection(
                        title = "6. Хрустальный Купол (Фирмамент)",
                        content = "Купол смыкается над диском с вершиной в зените Северного полюса (высота ~6500 км) и опирается на периметр Антарктической стены. Звездный свод вращается как единое целое вокруг Полярной звезды (Polaris) за один сидерический день (23 ч 56 мин 4 с).",
                        formula = "T_{сидерич} = 86 164.1 с\n" +
                                "z_{купола}(r) = H_{перим} + (H_{зенит} - H_{перим}) · (1 - (r / R)²)²"
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun FormulaSection(
    title: String,
    content: String,
    formula: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132042)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xFF070D1F),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formula,
                    color = Color(0xFFFDE047),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
