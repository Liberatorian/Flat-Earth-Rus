package com.example.model

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Constants and mathematical formulas for the Flat Earth celestial model.
 * Based on historical zetetic astronomy and azimuthal equidistant geometry.
 */
object FlatEarthConstants {
    // Disc dimensions (Scale reference)
    const val DISC_RADIUS_KM = 20000.0 // Distance from North Pole to outer Ice Wall
    const val EQUATOR_RADIUS_KM = 10000.0 // Equator circle
    const val TROPIC_CANCER_KM = 7400.0 // Inner solstice circle (+23.44°)
    const val TROPIC_CAPRICORN_KM = 12600.0 // Outer solstice circle (-23.44°)
    const val ARCTIC_CIRCLE_KM = 2600.0
    const val ANTARCTIC_CIRCLE_KM = 17400.0

    // Firmament / Celestial heights
    const val DOME_ZENITH_HEIGHT_KM = 10500.0 // Apex height that contains the full luminary orbital band
    const val DOME_RIM_HEIGHT_KM = 0.0 // Dome is anchored to the physical surface at the rim
    const val ICE_WALL_HEIGHT_METERS = 75.0 // Average ice cliff elevation above ocean (~250 ft)
    const val SUN_ALTITUDE_KM = 4800.0 // Local Sun altitude above flat plane (~3000 miles)
    const val MOON_ALTITUDE_KM = 4750.0 // Local Moon altitude (~3000 miles)
    const val SUN_DIAMETER_KM = 52.0 // Standard historical estimate (~32 miles)
    const val MOON_DIAMETER_KM = 52.0

    // Optics & Illumination
    const val SUN_SPOTLIGHT_RADIUS_KM = 9600.0 // Daytime spotlight illumination radius
    const val TWILIGHT_BUFFER_KM = 1800.0 // Penumbra / twilight transition zone

    fun domeHeightAtRadiusKm(radiusNorm: Double): Double {
        val radius = radiusNorm.coerceIn(0.0, 1.0)
        return DOME_RIM_HEIGHT_KM + (DOME_ZENITH_HEIGHT_KM - DOME_RIM_HEIGHT_KM) * (1.0 - radius * radius)
    }
}

data class DomePhysicalSettings(
    val sunAltitudeKm: Double = FlatEarthConstants.SUN_ALTITUDE_KM,
    val moonAltitudeKm: Double = FlatEarthConstants.MOON_ALTITUDE_KM,
    val domeZenithHeightKm: Double = FlatEarthConstants.DOME_ZENITH_HEIGHT_KM,
    val iceWallHeightVisualKm: Double = FlatEarthConstants.ICE_WALL_HEIGHT_METERS / 1000.0, // Physical wall height, no visual exaggeration
    val sunSpotlightRadiusKm: Double = FlatEarthConstants.SUN_SPOTLIGHT_RADIUS_KM
)

data class CityLocation(
    val nameRu: String,
    val nameEn: String,
    val latitude: Double, // degrees North (+) or South (-)
    val longitude: Double, // degrees East (+) or West (-)
    val description: String
)

val PRESET_CITIES = listOf(
    CityLocation("Москва", "Moscow", 55.7558, 37.6173, "Евразия, Россия (55.8°N, 37.6°E)"),
    CityLocation("Санкт-Петербург", "Saint Petersburg", 59.9343, 30.3351, "Северная Пальмира (59.9°N, 30.3°E)"),
    CityLocation("Лондон", "London", 51.5074, -0.1278, "Гринвичский меридиан 0° (51.5°N, 0.1°W)"),
    CityLocation("Нью-Йорк", "New York", 40.7128, -74.0060, "Северная Америка (40.7°N, 74.0°W)"),
    CityLocation("Токио", "Tokyo", 35.6762, 139.6503, "Восточная Азия (35.7°N, 139.7°E)"),
    CityLocation("Каир", "Cairo", 30.0444, 31.2357, "Северная Африка (30.0°N, 31.2°E)"),
    CityLocation("Сидней", "Sydney", -33.8688, 151.2093, "Австралия за экватором (33.9°S, 151.2°E)"),
    CityLocation("Кейптаун", "Cape Town", -33.9249, 18.4241, "Южная Африка (33.9°S, 18.4°E)"),
    CityLocation("Рио-де-Жанейро", "Rio de Janeiro", -22.9068, -43.1729, "Южная Америка (22.9°S, 43.2°W)"),
    CityLocation("Северный Полюс", "North Pole", 90.0, 0.0, "Центр диска и зенит Полярной звезды"),
    CityLocation("Ледяная Стена (Антарктида)", "Ice Wall Rim", -75.0, 0.0, "Внешний ледяной барьер тверди")
)

data class StarInfo(
    val name: String,
    val nameLatin: String,
    val constellationRu: String,
    val constellationLatin: String,
    val rightAscensionHours: Float, // 0..24h
    val declinationDeg: Float, // -80..+90
    val magnitude: Float,
    val colorHex: Long = 0xFFFFFFFF,
    val description: String = ""
)

data class ConstellationInfo(
    val nameRu: String,
    val nameLatin: String,
    val centerRAHours: Float,
    val centerDecDeg: Float,
    val descriptionRu: String
)

val CONSTELLATIONS_LIST = listOf(
    ConstellationInfo("Большая Медведица", "Ursa Major", 11.5f, 55.0f, "Главный северный ориентир. Семь ярких звезд Большого Ковша."),
    ConstellationInfo("Малая Медведица", "Ursa Minor", 15.0f, 75.0f, "Околополярное созвездие с Полярной звездой в зените вращения купола."),
    ConstellationInfo("Кассиопея", "Cassiopeia", 1.0f, 60.0f, "Характерная небесная корона или буква 'W' напротив Большой Медведицы."),
    ConstellationInfo("Орион", "Orion", 5.5f, 0.0f, "Величественный небесный охотник на небесном экваторе с ярким трио Пояса."),
    ConstellationInfo("Лебедь", "Cygnus", 20.5f, 42.0f, "Северный Крест, летящий вдоль Млечного Пути."),
    ConstellationInfo("Лира", "Lyra", 18.8f, 36.0f, "Компактное созвездие с ослепительной голубоватой Вегой."),
    ConstellationInfo("Орел", "Aquila", 19.8f, 3.0f, "Летне-осеннее созвездие с ярким Альтаиром."),
    ConstellationInfo("Телец", "Taurus", 4.5f, 18.0f, "Зодиакальное созвездие с оранжевым гигантом Альдебараном и Плеядами."),
    ConstellationInfo("Большой Пес", "Canis Major", 6.8f, -22.0f, "Южное созвездие, дом Сириуса — ярчайшей звезды земного неба."),
    ConstellationInfo("Волопас", "Boötes", 14.5f, 25.0f, "Пастух со сверкающим оранжевым Арктуром."),
    ConstellationInfo("Скорпион", "Scorpius", 16.8f, -30.0f, "Зодиакальное созвездие с красным Антаресом."),
    ConstellationInfo("Южный Крест", "Crux", 12.5f, -60.0f, "Знаменитый южный небесный ориентир из четырех ярких звезд."),
    ConstellationInfo("Центавр", "Centaurus", 14.0f, -50.0f, "Огромное южное созвездие с Толиманом (Альфа Центавра)."),
    ConstellationInfo("Лев", "Leo", 10.5f, 15.0f, "Весеннее зодиакальное созвездие в форме царственного льва с Регулом."),
    ConstellationInfo("Пегас", "Pegasus", 22.5f, 20.0f, "Крылатый конь с Большим Небесным Квадратом.")
)

val MAJOR_STARS = listOf(
    // 1. Полярный центр вращения (Малая Медведица)
    StarInfo("Полярная (Polaris)", "Alpha Ursae Minoris", "Малая Медведица", "Ursa Minor", 2.53f, 89.26f, 1.98f, 0xFFFFFBEB, "Неподвижный центр вращения небесного свода прямо над Северным полюсом"),
    StarInfo("Кохаб (Kochab)", "Beta Ursae Minoris", "Малая Медведица", "Ursa Minor", 14.85f, 74.16f, 2.08f, 0xFFFFEDD5, "Оранжевый страж Северного полюса"),
    StarInfo("Феркад (Pherkad)", "Gamma Ursae Minoris", "Малая Медведица", "Ursa Minor", 15.35f, 71.83f, 3.05f, 0xFFFFFFFF, "Второй страж полюса"),

    // 2. Большая Медведица (Семь звезд ковша)
    StarInfo("Дубхе (Dubhe)", "Alpha Ursae Majoris", "Большая Медведица", "Ursa Major", 11.06f, 61.75f, 1.79f, 0xFFFED7AA, "Указатель на Полярную звезду"),
    StarInfo("Мерак (Merak)", "Beta Ursae Majoris", "Большая Медведица", "Ursa Major", 11.03f, 56.38f, 2.37f, 0xFFFFFFFF, "Нижний указатель ковша на Полярную"),
    StarInfo("Фекда (Phecda)", "Gamma Ursae Majoris", "Большая Медведица", "Ursa Major", 11.90f, 53.69f, 2.44f, 0xFFFFFFFF, "Основание чаши Большого Ковша"),
    StarInfo("Мегрец (Megrez)", "Delta Ursae Majoris", "Большая Медведица", "Ursa Major", 12.25f, 57.03f, 3.31f, 0xFFFFFFFF, "Соединение ручки и ковша"),
    StarInfo("Алиот (Alioth)", "Epsilon Ursae Majoris", "Большая Медведица", "Ursa Major", 12.90f, 55.96f, 1.77f, 0xFFFFFFFF, "Самая яркая звезда Большой Медведицы"),
    StarInfo("Мицар (Mizar)", "Zeta Ursae Majoris", "Большая Медведица", "Ursa Major", 13.40f, 54.92f, 2.23f, 0xFFFFFFFF, "Знаменитая двойная звезда с Алькором"),
    StarInfo("Алькаид (Alkaid)", "Eta Ursae Majoris", "Большая Медведица", "Ursa Major", 13.80f, 49.31f, 1.86f, 0xFFBAE6FD, "Кончик рукояти Большого Ковша"),

    // 3. Кассиопея (W-астеризм)
    StarInfo("Шедар (Schedar)", "Alpha Cassiopeiae", "Кассиопея", "Cassiopeia", 0.67f, 56.54f, 2.23f, 0xFFFED7AA, "Оранжевый гигант в сердцевине Кассиопеи"),
    StarInfo("Каф (Caph)", "Beta Cassiopeiae", "Кассиопея", "Cassiopeia", 0.15f, 59.15f, 2.27f, 0xFFFFFFFF, "Правый край буквы W"),
    StarInfo("Нави (Navi)", "Gamma Cassiopeiae", "Кассиопея", "Cassiopeia", 0.94f, 60.72f, 2.15f, 0xFFBAE6FD, "Центральный зубец короны Кассиопеи"),
    StarInfo("Рукбах (Ruchbah)", "Delta Cassiopeiae", "Кассиопея", "Cassiopeia", 1.43f, 60.23f, 2.68f, 0xFFFFFFFF, "Колено королевы Кассиопеи"),
    StarInfo("Сегин (Segin)", "Epsilon Cassiopeiae", "Кассиопея", "Cassiopeia", 1.90f, 63.67f, 3.35f, 0xFFBAE6FD, "Левый край буквы W"),

    // 4. Летне-осенний треугольник (Вега, Денеб, Альтаир)
    StarInfo("Вега (Vega)", "Alpha Lyrae", "Лира", "Lyra", 18.62f, 38.78f, 0.03f, 0xFFBAE6FD, "Ослепительная голубовато-белая жемчужина Лиры"),
    StarInfo("Денеб (Deneb)", "Alpha Cygni", "Лебедь", "Cygnus", 20.69f, 45.28f, 1.25f, 0xFFFFFFFF, "Белый сверхгигант в хвосте Лебедя"),
    StarInfo("Садр (Sadr)", "Gamma Cygni", "Лебедь", "Cygnus", 20.37f, 40.26f, 2.23f, 0xFFFEF3C7, "Центр перекрестия Северного Креста"),
    StarInfo("Альбирео (Albireo)", "Beta Cygni", "Лебедь", "Cygnus", 19.51f, 27.96f, 3.05f, 0xFFFBBF24, "Великолепная контрастная двойная звезда"),
    StarInfo("Альтаир (Altair)", "Alpha Aquilae", "Орел", "Aquila", 19.85f, 8.87f, 0.77f, 0xFFFFFFFF, "Быстро вращающаяся сплюснутая звезда Орла"),

    // 5. Орион и окрестности
    StarInfo("Бетельгейзе (Betelgeuse)", "Alpha Orionis", "Орион", "Orion", 5.92f, 7.41f, 0.50f, 0xFFF87171, "Красный сверхгигант в плече Ориона"),
    StarInfo("Ригель (Rigel)", "Beta Orionis", "Орион", "Orion", 5.24f, -8.20f, 0.13f, 0xFFBAE6FD, "Голубой сверхгигант в левой ноге Ориона"),
    StarInfo("Беллатрикс (Bellatrix)", "Gamma Orionis", "Орион", "Orion", 5.42f, 6.35f, 1.64f, 0xFFBAE6FD, "Звезда-амазонка правого плеча"),
    StarInfo("Саиф (Saiph)", "Kappa Orionis", "Орион", "Orion", 5.79f, -9.67f, 2.07f, 0xFF93C5FD, "Правая стопа небесного охотника"),
    StarInfo("Минтака (Mintaka)", "Delta Orionis", "Орион", "Orion", 5.53f, -0.30f, 2.23f, 0xFFBAE6FD, "Западная звезда пояса Ориона на небесном экваторе"),
    StarInfo("Альнилам (Alnilam)", "Epsilon Orionis", "Орион", "Orion", 5.60f, -1.20f, 1.69f, 0xFFBAE6FD, "Центральная жемчужина пояса Ориона"),
    StarInfo("Альнитак (Alnitak)", "Zeta Orionis", "Орион", "Orion", 5.68f, -1.94f, 1.77f, 0xFFBAE6FD, "Восточная звезда пояса Ориона"),

    // 6. Сириус и Большой Пес
    StarInfo("Сириус (Sirius)", "Alpha Canis Majoris", "Большой Пес", "Canis Major", 6.75f, -16.72f, -1.46f, 0xFFE0F2FE, "Ярчайшая звезда ночного неба Земли"),
    StarInfo("Адара (Adhara)", "Epsilon Canis Majoris", "Большой Пес", "Canis Major", 6.98f, -28.97f, 1.50f, 0xFFBAE6FD, "Голубой гигант в созвездии Большого Пса"),

    // 7. Телец и Плеяды
    StarInfo("Альдебаран (Aldebaran)", "Alpha Tauri", "Телец", "Taurus", 4.60f, 16.51f, 0.86f, 0xFFFB923C, "Пылающий оранжевый глаз Тельца"),
    StarInfo("Эльнат (Elnath)", "Beta Tauri", "Телец", "Taurus", 5.44f, 28.61f, 1.65f, 0xFFBAE6FD, "Северный рог Тельца"),

    // 8. Возничий и Волопас
    StarInfo("Капелла (Capella)", "Alpha Aurigae", "Возничий", "Auriga", 5.28f, 45.99f, 0.08f, 0xFFFEF08A, "Яркая золотисто-желтая кратная звезда"),
    StarInfo("Арктур (Arcturus)", "Alpha Boötis", "Волопас", "Boötes", 14.26f, 19.18f, -0.05f, 0xFFFDBA74, "Оранжевый гигант, одна из ярчайших звезд неба"),

    // 9. Скорпион и Дева
    StarInfo("Антарес (Antares)", "Alpha Scorpii", "Скорпион", "Scorpius", 16.49f, -26.43f, 1.06f, 0xFFEF4444, "Красный супергигант, 'Соперник Марса'"),
    StarInfo("Спика (Spica)", "Alpha Virginis", "Дева", "Virgo", 13.42f, -11.16f, 0.98f, 0xFFBAE6FD, "Голубая жемчужина зодиакального созвездия Девы"),

    // 10. Южное небо: Южный Крест, Центавр, Киль
    StarInfo("Акрукс (Acrux)", "Alpha Crucis", "Южный Крест", "Crux", 12.44f, -63.10f, 0.76f, 0xFFBAE6FD, "Нижняя ярчайшая звезда Южного Креста"),
    StarInfo("Мимоза (Becrux)", "Beta Crucis", "Южный Крест", "Crux", 12.79f, -59.69f, 1.25f, 0xFFBAE6FD, "Восточная звезда Южного Креста"),
    StarInfo("Гакрукс (Gacrux)", "Gamma Crucis", "Южный Крест", "Crux", 12.52f, -57.11f, 1.63f, 0xFFFCA5A5, "Красный гигант, вершина Южного Креста"),
    StarInfo("Канопус (Canopus)", "Alpha Carinae", "Киль", "Carina", 6.40f, -52.70f, -0.74f, 0xFFFFFBEB, "Вторая по яркости звезда ночного неба"),
    StarInfo("Ригил Кентаврус", "Alpha Centauri", "Центавр", "Centaurus", 14.66f, -60.83f, -0.27f, 0xFFFEF08A, "Альфа Центавра — ближайшая звездная система"),
    StarInfo("Хадар (Hadar)", "Beta Centauri", "Центавр", "Centaurus", 14.06f, -60.37f, 0.61f, 0xFFBAE6FD, "Голубовато-белый гигант в Центавре"),

    // 11. Лев и Пегас
    StarInfo("Регул (Regulus)", "Alpha Leonis", "Лев", "Leo", 10.14f, 11.97f, 1.35f, 0xFFBAE6FD, "Сердце Льва, царственная звезда"),
    StarInfo("Денебола (Denebola)", "Beta Leonis", "Лев", "Leo", 11.82f, 14.57f, 2.14f, 0xFFFFFFFF, "Хвост Льва"),
    StarInfo("Маркаб (Markab)", "Alpha Pegasi", "Пегас", "Pegasus", 23.08f, 15.21f, 2.49f, 0xFFBAE6FD, "Вершина Большого Квадрата Пегаса"),
    StarInfo("Шеат (Scheat)", "Beta Pegasi", "Пегас", "Pegasus", 23.06f, 28.08f, 2.42f, 0xFFFED7AA, "Красный гигант в квадрате Пегаса"),

    // Дополнительные ориентиры: Близнецы, Малый Пёс, Дева и Козерог
    StarInfo("Кастор (Castor)", "Alpha Geminorum", "Близнецы", "Gemini", 7.58f, 31.89f, 1.58f, 0xFFBAE6FD, "Двойная звезда северного зодиака"),
    StarInfo("Поллукс (Pollux)", "Beta Geminorum", "Близнецы", "Gemini", 7.76f, 28.03f, 1.14f, 0xFFFFD59A, "Золотистая звезда Близнецов"),
    StarInfo("Процион (Procyon)", "Alpha Canis Minoris", "Малый Пёс", "Canis Minor", 7.66f, 5.23f, 0.34f, 0xFFFFF4C2, "Яркая звезда Малого Пса"),
    StarInfo("Поррима (Porrima)", "Gamma Virginis", "Дева", "Virgo", 12.69f, -1.45f, 2.74f, 0xFFFFFFFF, "Двойная звезда Девы"),
    StarInfo("Зубенэльгенуби (Zubenelgenubi)", "Alpha Librae", "Весы", "Libra", 14.85f, -16.04f, 2.75f, 0xFFFFE4B5, "Южная клешня древнего Скорпиона"),
    StarInfo("Денеб Альгеди (Deneb Algedi)", "Delta Capricorni", "Козерог", "Capricornus", 21.78f, -16.13f, 2.85f, 0xFFFFFFFF, "Хвост Козерога")
)

data class ConstellationLine(
    val star1: String,
    val star2: String
)

val CONSTELLATION_LINES = listOf(
    // Большая Медведица (Ковш)
    ConstellationLine("Дубхе (Dubhe)", "Мерак (Merak)"),
    ConstellationLine("Мерак (Merak)", "Фекда (Phecda)"),
    ConstellationLine("Фекда (Phecda)", "Мегрец (Megrez)"),
    ConstellationLine("Мегрец (Megrez)", "Дубхе (Dubhe)"),
    ConstellationLine("Мегрец (Megrez)", "Алиот (Alioth)"),
    ConstellationLine("Алиот (Alioth)", "Мицар (Mizar)"),
    ConstellationLine("Мицар (Mizar)", "Алькаид (Alkaid)"),
    // Указатель на Полярную
    ConstellationLine("Мерак (Merak)", "Дубхе (Dubhe)"),
    ConstellationLine("Дубхе (Dubhe)", "Полярная (Polaris)"),

    // Малая Медведица
    ConstellationLine("Полярная (Polaris)", "Феркад (Pherkad)"),
    ConstellationLine("Феркад (Pherkad)", "Кохаб (Kochab)"),

    // Кассиопея (W)
    ConstellationLine("Каф (Caph)", "Шедар (Schedar)"),
    ConstellationLine("Шедар (Schedar)", "Нави (Navi)"),
    ConstellationLine("Нави (Navi)", "Рукбах (Ruchbah)"),
    ConstellationLine("Рукбах (Ruchbah)", "Сегин (Segin)"),

    // Лебедь (Северный Крест)
    ConstellationLine("Денеб (Deneb)", "Садр (Sadr)"),
    ConstellationLine("Садр (Sadr)", "Альбирео (Albireo)"),

    // Летний треугольник
    ConstellationLine("Вега (Vega)", "Денеб (Deneb)"),
    ConstellationLine("Денеб (Deneb)", "Альтаир (Altair)"),
    ConstellationLine("Альтаир (Altair)", "Вега (Vega)"),

    // Орион
    ConstellationLine("Бетельгейзе (Betelgeuse)", "Беллатрикс (Bellatrix)"),
    ConstellationLine("Бетельгейзе (Betelgeuse)", "Альнитак (Alnitak)"),
    ConstellationLine("Беллатрикс (Bellatrix)", "Минтака (Mintaka)"),
    ConstellationLine("Минтака (Mintaka)", "Альнилам (Alnilam)"),
    ConstellationLine("Альнилам (Alnilam)", "Альнитак (Alnitak)"),
    ConstellationLine("Альнитак (Alnitak)", "Саиф (Saiph)"),
    ConstellationLine("Минтака (Mintaka)", "Ригель (Rigel)"),
    ConstellationLine("Саиф (Saiph)", "Ригель (Rigel)"),

    // Соединения с соседними звездами
    ConstellationLine("Бетельгейзе (Betelgeuse)", "Альдебаран (Aldebaran)"),
    ConstellationLine("Альдебаран (Aldebaran)", "Эльнат (Elnath)"),
    ConstellationLine("Альдебаран (Aldebaran)", "Капелла (Capella)"),
    ConstellationLine("Альнилам (Alnilam)", "Сириус (Sirius)"),
    ConstellationLine("Сириус (Sirius)", "Адара (Adhara)"),

    // Волопас и Дева
    ConstellationLine("Алькаид (Alkaid)", "Арктур (Arcturus)"),
    ConstellationLine("Арктур (Arcturus)", "Спика (Spica)"),

    // Лев
    ConstellationLine("Регул (Regulus)", "Денебола (Denebola)"),

    // Пегас
    ConstellationLine("Маркаб (Markab)", "Шеат (Scheat)"),

    // Близнецы
    ConstellationLine("Кастор (Castor)", "Поллукс (Pollux)"),
    ConstellationLine("Поллукс (Pollux)", "Процион (Procyon)"),

    // Дева и южная часть зодиака
    ConstellationLine("Спика (Spica)", "Поррима (Porrima)"),
    ConstellationLine("Поррима (Porrima)", "Зубенэльгенуби (Zubenelgenubi)"),
    ConstellationLine("Зубенэльгенуби (Zubenelgenubi)", "Антарес (Antares)"),
    ConstellationLine("Антарес (Antares)", "Денеб Альгеди (Deneb Algedi)"),

    // Южный Крест
    ConstellationLine("Акрукс (Acrux)", "Гакрукс (Gacrux)"),
    ConstellationLine("Мимоза (Becrux)", "Гакрукс (Gacrux)"),
    ConstellationLine("Мимоза (Becrux)", "Акрукс (Acrux)"),
    ConstellationLine("Ригил Кентаврус", "Хадар (Hadar)")
)
