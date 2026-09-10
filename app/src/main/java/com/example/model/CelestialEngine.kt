package com.example.model

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CelestialTelemetry(
    // Time
    val timestampMillis: Long,
    val utcHourString: String,
    val localHourString: String,
    val dayOfYear: Int,
    val seasonNameRu: String,

    // Sun Flat Earth Telemetry
    val sunLatitude: Double, // Subsolar latitude (declination)
    val sunLongitude: Double, // Subsolar longitude
    val sunDiscRadiusKm: Double, // Radius from North Pole
    val sunAltitudeKm: Double, // Height above plane (~5000 km)
    val sunSpeedKmH: Double, // Tangential orbital velocity
    val sunDiscX: Double, // Normalized -1..1
    val sunDiscY: Double, // Normalized -1..1

    // Moon Flat Earth Telemetry
    val moonLatitude: Double,
    val moonLongitude: Double,
    val moonDiscRadiusKm: Double,
    val moonAltitudeKm: Double,
    val moonPhaseFraction: Float, // 0.0 (new) .. 1.0 (full) .. 0.0
    val moonPhaseAngleDeg: Double, // 0..360
    val moonPhaseNameRu: String,
    val moonAgeDays: Double,
    val moonDiscX: Double,
    val moonDiscY: Double,

    // Observer Telemetry (from chosen city/lat/lon)
    val observerCityName: String,
    val observerLat: Double,
    val observerLon: Double,
    val observerDiscX: Double,
    val observerDiscY: Double,
    val isObserverDaytime: Boolean,
    val observerDistanceToSunKm: Double,
    val observerDistanceToMoonKm: Double,

    // Observer Sky Angles (Altitude & Azimuth from perspective)
    val sunGeometricAltitudeDeg: Double,
    val sunApparentAltitudeDeg: Double, // Elevation above horizon after optical path
    val sunApparentAzimuthDeg: Double, // Compass bearing (0=N, 90=E, 180=S, 270=W)
    val sunApparentDiameterArcmin: Double,
    val moonGeometricAltitudeDeg: Double,
    val moonApparentAltitudeDeg: Double,
    val moonApparentAzimuthDeg: Double,
    val moonApparentDiameterArcmin: Double,

    // Firmament / Dome Rotation
    val starDomeRotationDeg: Float
)

object CelestialEngine {

    /** Greenwich mean sidereal time for the supplied UTC instant. */
    fun greenwichMeanSiderealTimeDegrees(timestampMillis: Long): Double {
        val julianDate = timestampMillis / 86400000.0 + 2440587.5
        val daysSinceJ2000 = julianDate - 2451545.0
        val gmstHours = (18.697374558 + 24.06570982441908 * daysSinceJ2000) % 24.0
        return (gmstHours * 15.0 + 360.0) % 360.0
    }

    /**
     * Convert geographic (lat, lon) to Flat Earth Azimuthal Equidistant coordinates.
     * North Pole (90°N) -> (0, 0)
     * Equator (0°) -> r = 10,000 km
     * Outer Rim (-90°S) -> r = 20,000 km
     */
    fun geoToDiscKm(lat: Double, lon: Double): Pair<Double, Double> {
        return geoToDiscKm(lat, lon, MapProjection.GLEASON_AE)
    }

    fun geoToDiscKm(lat: Double, lon: Double, projection: MapProjection): Pair<Double, Double> {
        val colatitude = (90.0 - lat).coerceIn(0.0, 180.0)
        val colatitudeRad = Math.toRadians(colatitude)
        val r = when (projection) {
            MapProjection.GLEASON_AE -> (colatitude / 180.0) * FlatEarthConstants.DISC_RADIUS_KM
            MapProjection.STEREOGRAPHIC -> {
                val normalized = kotlin.math.tan(colatitudeRad / 2.0) / kotlin.math.tan(Math.toRadians(89.0))
                normalized.coerceIn(0.0, 1.0) * FlatEarthConstants.DISC_RADIUS_KM
            }
            MapProjection.ORTHOGRAPHIC -> sin(colatitudeRad) * FlatEarthConstants.DISC_RADIUS_KM
        }
        val lonRad = Math.toRadians(lon)
        val x = r * sin(lonRad)
        val y = -r * cos(lonRad)
        return Pair(x, y)
    }

    /**
     * Compute complete celestial and observer state for a given timestamp, observer location,
     * and physical dome parameters.
     */
    fun calculateState(
        timestampMillis: Long,
        observerCity: CityLocation,
        physicalSettings: DomePhysicalSettings = DomePhysicalSettings()
    ): CelestialTelemetry {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = timestampMillis
        }

        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val hourUtc = cal.get(Calendar.HOUR_OF_DAY)
        val minUtc = cal.get(Calendar.MINUTE)
        val secUtc = cal.get(Calendar.SECOND)
        val msUtc = cal.get(Calendar.MILLISECOND)

        val fracHourUtc = hourUtc + (minUtc / 60.0) + (secUtc / 3600.0) + (msUtc / 3600000.0)
        val utcHourString = String.format("%02d:%02d:%02d UTC", hourUtc, minUtc, secUtc)
        val localCalendar = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        val localHourString = String.format(
            "%02d:%02d:%02d %s",
            localCalendar.get(Calendar.HOUR_OF_DAY),
            localCalendar.get(Calendar.MINUTE),
            localCalendar.get(Calendar.SECOND),
            localCalendar.timeZone.id
        )

        // 1. Solar Declination (Seasonal spiral between +23.44° and -23.44°)
        // Day 80 is approx Spring Equinox (March 21)
        val seasonalPhase = 2.0 * PI * (dayOfYear - 80.0) / 365.25
        val sunDeclinationDeg = 23.44 * sin(seasonalPhase)

        val seasonName = when {
            sunDeclinationDeg > 18.0 -> "Летнее солнцестояние (Север)"
            sunDeclinationDeg > 5.0 -> "Поздняя весна / Раннее лето"
            sunDeclinationDeg > -5.0 && sin(seasonalPhase) > 0 -> "Весеннее равноденствие"
            sunDeclinationDeg > -5.0 -> "Осеннее равноденствие"
            sunDeclinationDeg < -18.0 -> "Зимнее солнцестояние (Юг)"
            else -> "Осень / Зима"
        }

        // Subsolar radial distance from North Pole
        val sunRadiusKm = (90.0 - sunDeclinationDeg) / 180.0 * FlatEarthConstants.DISC_RADIUS_KM

        // Subsolar longitude: at 12:00 UTC Sun is at Greenwich meridian (0°)
        // Moves westwards 15° per hour
        val sunLonDeg = -((fracHourUtc - 12.0) * 15.0).let {
            var l = it % 360.0
            if (l < -180.0) l += 360.0
            if (l > 180.0) l -= 360.0
            l
        }

        val (sunXKm, sunYKm) = geoToDiscKm(sunDeclinationDeg, sunLonDeg)
        val sunDiscX = sunXKm / FlatEarthConstants.DISC_RADIUS_KM
        val sunDiscY = sunYKm / FlatEarthConstants.DISC_RADIUS_KM

        // Orbital speed on Flat Earth: v = 2 * pi * r / 24 hours
        val sunSpeedKmH = (2.0 * PI * sunRadiusKm) / 24.0

        // 2. Moon Mechanics
        // Synodic period = 29.530589 days
        // Diurnal rotation period = ~24.84 hours (lags ~50 min per day)
        val daysSinceEpoch = timestampMillis / (1000.0 * 86400.0)
        val synodicDays = (daysSinceEpoch % 29.530589 + 29.530589) % 29.530589
        val phaseAngleRad = (synodicDays / 29.530589) * 2.0 * PI
        val phaseAngleDeg = Math.toDegrees(phaseAngleRad)
        val phaseFraction = ((1.0 - cos(phaseAngleRad)) / 2.0).toFloat()

        val moonPhaseName = when {
            phaseAngleDeg < 15.0 || phaseAngleDeg >= 345.0 -> "Новолуние (0%)"
            phaseAngleDeg < 75.0 -> "Растущий серп (${(phaseFraction * 100).toInt()}%)"
            phaseAngleDeg < 105.0 -> "Первая четверть (50%)"
            phaseAngleDeg < 165.0 -> "Растущая Луна (${(phaseFraction * 100).toInt()}%)"
            phaseAngleDeg < 195.0 -> "Полнолуние (100%)"
            phaseAngleDeg < 255.0 -> "Убывающая Луна (${(phaseFraction * 100).toInt()}%)"
            phaseAngleDeg < 285.0 -> "Последняя четверть (50%)"
            else -> "Стареющий месяц (${(phaseFraction * 100).toInt()}%)"
        }

        // The Moon follows the Sun around the disc, advancing eastward by its
        // phase angle. This keeps its apparent daily motion slightly slower.
        var moonLonDeg = (sunLonDeg + phaseAngleDeg) % 360.0
        if (moonLonDeg < -180.0) moonLonDeg += 360.0
        if (moonLonDeg > 180.0) moonLonDeg -= 360.0

        // Moon radial position oscillates slightly around sunRadius
        val moonDeclinationDeg = (sunDeclinationDeg + 5.14 * sin(phaseAngleRad * 2)).coerceIn(-28.5, 28.5)
        val moonRadiusKm = (90.0 - moonDeclinationDeg) / 180.0 * FlatEarthConstants.DISC_RADIUS_KM
        val (moonXKm, moonYKm) = geoToDiscKm(moonDeclinationDeg, moonLonDeg)
        val moonDiscX = moonXKm / FlatEarthConstants.DISC_RADIUS_KM
        val moonDiscY = moonYKm / FlatEarthConstants.DISC_RADIUS_KM

        // 3. Observer Location & Perspective Telemetry
        val (obsXKm, obsYKm) = geoToDiscKm(observerCity.latitude, observerCity.longitude)
        val obsDiscX = obsXKm / FlatEarthConstants.DISC_RADIUS_KM
        val obsDiscY = obsYKm / FlatEarthConstants.DISC_RADIUS_KM

        // Distance on plane to Sun
        val dxSun = sunXKm - obsXKm
        val dySun = sunYKm - obsYKm
        val horizDistSunKm = sqrt(dxSun * dxSun + dySun * dySun)
        val totalDistSunKm = sqrt(horizDistSunKm * horizDistSunKm + physicalSettings.sunAltitudeKm * physicalSettings.sunAltitudeKm)

        // Day/Night determination based on Sun spotlight radius
        val isDaytime = horizDistSunKm <= physicalSettings.sunSpotlightRadiusKm

        // Distance on plane to Moon
        val dxMoon = moonXKm - obsXKm
        val dyMoon = moonYKm - obsYKm
        val horizDistMoonKm = sqrt(dxMoon * dxMoon + dyMoon * dyMoon)
        val totalDistMoonKm = sqrt(horizDistMoonKm * horizDistMoonKm + physicalSettings.moonAltitudeKm * physicalSettings.moonAltitudeKm)

        // Perspective Elevation (Altitude)
        // Law of perspective: tan(alt) = H / d_horiz
        val sunAltDeg = Math.toDegrees(atan2(physicalSettings.sunAltitudeKm, horizDistSunKm))
        val moonAltDeg = Math.toDegrees(atan2(physicalSettings.moonAltitudeKm, horizDistMoonKm))
        val sunOpticalPath = OpticsEngine.traceIncomingRay(sunAltDeg)
        val moonOpticalPath = OpticsEngine.traceIncomingRay(moonAltDeg)

        // Perspective Apparent Diameter: theta = 2 * atan(D / (2 * dist))
        val sunDiameterArcmin = 2.0 * atan2(FlatEarthConstants.SUN_DIAMETER_KM / 2.0, totalDistSunKm) * (180.0 / PI) * 60.0
        val moonDiameterArcmin = 2.0 * atan2(FlatEarthConstants.MOON_DIAMETER_KM / 2.0, totalDistMoonKm) * (180.0 / PI) * 60.0

        // Local Compass Azimuth from Observer
        // On Flat Earth polar map:
        // Local North vector points from observer towards North Pole (0, 0): (-obsX, -obsY)
        // Local East vector points 90° clockwise from North or counter-clockwise tangent: (obsY, -obsX)
        val obsDistFromPole = sqrt(obsXKm * obsXKm + obsYKm * obsYKm)
        val (sunAzimuthDeg, moonAzimuthDeg) = if (obsDistFromPole > 10.0) {
            val northX = -obsXKm / obsDistFromPole
            val northY = -obsYKm / obsDistFromPole
            val eastX = -northY // 90° to right of North
            val eastY = northX

            // Project Sun vector onto North and East
            val sunProjNorth = dxSun * northX + dySun * northY
            val sunProjEast = dxSun * eastX + dySun * eastY
            var sunAz = Math.toDegrees(atan2(sunProjEast, sunProjNorth))
            if (sunAz < 0) sunAz += 360.0

            val moonProjNorth = dxMoon * northX + dyMoon * northY
            val moonProjEast = dxMoon * eastX + dyMoon * eastY
            var moonAz = Math.toDegrees(atan2(moonProjEast, moonProjNorth))
            if (moonAz < 0) moonAz += 360.0

            Pair(sunAz, moonAz)
        } else {
            // At North Pole, all directions are South; define azimuth relative to Greenwich
            var sunAz = Math.toDegrees(atan2(dxSun, -dySun))
            if (sunAz < 0) sunAz += 360.0
            var moonAz = Math.toDegrees(atan2(dxMoon, -dyMoon))
            if (moonAz < 0) moonAz += 360.0
            Pair(sunAz, moonAz)
        }

        // 4. Firmament / Star Dome rotation
        // 1 sidereal day = 23.9344696 hours (~86164 seconds)
        val siderealRotationDeg = greenwichMeanSiderealTimeDegrees(timestampMillis).toFloat()

        return CelestialTelemetry(
            timestampMillis = timestampMillis,
            utcHourString = utcHourString,
            localHourString = localHourString,
            dayOfYear = dayOfYear,
            seasonNameRu = seasonName,
            sunLatitude = sunDeclinationDeg,
            sunLongitude = sunLonDeg,
            sunDiscRadiusKm = sunRadiusKm,
            sunAltitudeKm = physicalSettings.sunAltitudeKm,
            sunSpeedKmH = sunSpeedKmH,
            sunDiscX = sunDiscX,
            sunDiscY = sunDiscY,
            moonLatitude = moonDeclinationDeg,
            moonLongitude = moonLonDeg,
            moonDiscRadiusKm = moonRadiusKm,
            moonAltitudeKm = physicalSettings.moonAltitudeKm,
            moonPhaseFraction = phaseFraction,
            moonPhaseAngleDeg = phaseAngleDeg,
            moonPhaseNameRu = moonPhaseName,
            moonAgeDays = synodicDays,
            moonDiscX = moonDiscX,
            moonDiscY = moonDiscY,
            observerCityName = observerCity.nameRu,
            observerLat = observerCity.latitude,
            observerLon = observerCity.longitude,
            observerDiscX = obsDiscX,
            observerDiscY = obsDiscY,
            isObserverDaytime = isDaytime,
            observerDistanceToSunKm = totalDistSunKm,
            observerDistanceToMoonKm = totalDistMoonKm,
            sunGeometricAltitudeDeg = sunAltDeg,
            sunApparentAltitudeDeg = sunOpticalPath.observedAltitudeDeg,
            sunApparentAzimuthDeg = sunAzimuthDeg,
            sunApparentDiameterArcmin = sunDiameterArcmin,
            moonGeometricAltitudeDeg = moonAltDeg,
            moonApparentAltitudeDeg = moonOpticalPath.observedAltitudeDeg,
            moonApparentAzimuthDeg = moonAzimuthDeg,
            moonApparentDiameterArcmin = moonDiameterArcmin,
            starDomeRotationDeg = siderealRotationDeg.toFloat()
        )
    }

    /**
     * Convert equatorial celestial coordinates (RA, Dec) to local horizontal coordinates (Alt, Az)
     * for a ground observer at a specific geographical location and timestamp.
     * Altitude: -90° (nadir) to +90° (zenith). >0° means visible above horizon!
     * Azimuth: 0° (North) -> 90° (East) -> 180° (South) -> 270° (West).
     */
    fun equatorialToHorizontal(
        raHours: Float,
        decDeg: Float,
        obsLatDeg: Double,
        obsLonDeg: Double,
        timestampMillis: Long
    ): Pair<Double, Double> {
        // Unix time modulo one day is not GMST because the sidereal clock has a
        // different period and a non-zero J2000 offset.
        val gmstDeg = greenwichMeanSiderealTimeDegrees(timestampMillis)
        var lstDeg = (gmstDeg + obsLonDeg) % 360.0
        if (lstDeg < 0) lstDeg += 360.0

        val starRaDeg = (raHours * 15.0).toDouble()
        var hourAngleDeg = (lstDeg - starRaDeg) % 360.0
        if (hourAngleDeg < 0) hourAngleDeg += 360.0

        val latRad = Math.toRadians(obsLatDeg)
        val decRad = Math.toRadians(decDeg.toDouble())
        val haRad = Math.toRadians(hourAngleDeg)

        // Altitude: sin(Alt) = sin(phi)*sin(delta) + cos(phi)*cos(delta)*cos(HA)
        val sinAlt = sin(latRad) * sin(decRad) + cos(latRad) * cos(decRad) * cos(haRad)
        val altDeg = Math.toDegrees(kotlin.math.asin(sinAlt.coerceIn(-1.0, 1.0)))

        // Azimuth:
        val y = -sin(haRad) * cos(decRad)
        val x = sin(decRad) * cos(latRad) - cos(decRad) * sin(latRad) * cos(haRad)
        var azDeg = Math.toDegrees(atan2(y, x))
        if (azDeg < 0) azDeg += 360.0

        return Pair(altDeg, azDeg)
    }
}
