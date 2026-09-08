package com.example.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ViewMode {
    DOME_3D, // Exterior 3D view: Disk, Ice Wall, Crystalline Dome, Orbiting Sun & Moon
    OBSERVER_SKY, // Interior Observer view: From ground looking at sky dome, horizon, altitude/azimuth
    GLEASON_2D // Top-down 2D Gleason Azimuthal Equidistant cartographic projection
}

enum class MapProjection {
    GLEASON_AE, // Standard Azimuthal Equidistant
    STEREOGRAPHIC, // Conformal Polar Stereographic
    ORTHOGRAPHIC // Orthographic Dome Perspective
}

data class LayerSettings(
    val showCoordinateGrid: Boolean = true,
    val showTropicsAndEquator: Boolean = true,
    val showDayNightCone: Boolean = true,
    val showSunBeam: Boolean = true,
    val showMoonBeam: Boolean = true,
    val showStarsAndConstellations: Boolean = true,
    val showCityMarkers: Boolean = true,
    val showMagneticLines: Boolean = false,
    val showFirmamentGlow: Boolean = true,
    val showOpticalRays: Boolean = true,
    val showScientificAssumptions: Boolean = true
)

data class Camera3D(
    val pitchDeg: Float = 35f, // Tilt angle (0=top down, 90=flat edge)
    val yawDeg: Float = 45f, // Azimuth rotation
    val zoom: Float = 1.0f // Scale factor
)

data class ObserverCamera(
    val azimuthHeadingDeg: Float = 180f, // Compass direction looking towards (180=South)
    val elevationPitchDeg: Float = 30f, // Looking up towards zenith (0=horizon, 90=zenith)
    val fovZoom: Float = 1.0f,
    val eyeHeightMeters: Double = 1.7
)

data class FlatEarthAppState(
    val currentTimestampMillis: Long = System.currentTimeMillis(),
    val isRealTime: Boolean = false,
    val isPlaying: Boolean = true,
    val timeSpeedMultiplier: Double = 60.0, // 60x = 1 minute per real second
    val viewMode: ViewMode = ViewMode.DOME_3D,
    val projection: MapProjection = MapProjection.GLEASON_AE,
    val observerLocation: CityLocation = PRESET_CITIES[0], // Moscow default
    val layers: LayerSettings = LayerSettings(),
    val camera3D: Camera3D = Camera3D(),
    val observerCamera: ObserverCamera = ObserverCamera(),
    val telemetry: CelestialTelemetry = CelestialEngine.calculateState(
        System.currentTimeMillis(),
        PRESET_CITIES[0]
    ),
    val showTheoryDialog: Boolean = false,
    val showLocationPicker: Boolean = false,
    val showSettingsSheet: Boolean = false
)

class FlatEarthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FlatEarthAppState())
    val uiState: StateFlow<FlatEarthAppState> = _uiState.asStateFlow()

    init {
        startSimulationTicker()
    }

    private fun startSimulationTicker() {
        viewModelScope.launch {
            var lastRealTime = System.currentTimeMillis()
            while (isActive) {
                delay(33) // ~30 fps update rate for buttery smooth celestial orbit
                val now = System.currentTimeMillis()
                val deltaRealMillis = now - lastRealTime
                lastRealTime = now

                _uiState.update { state ->
                    val newTimestamp = when {
                        state.isRealTime -> now
                        state.isPlaying -> (state.currentTimestampMillis + (deltaRealMillis * state.timeSpeedMultiplier)).toLong()
                        else -> state.currentTimestampMillis
                    }

                    val updatedTelemetry = CelestialEngine.calculateState(
                        newTimestamp,
                        state.observerLocation
                    )

                    state.copy(
                        currentTimestampMillis = newTimestamp,
                        telemetry = updatedTelemetry
                    )
                }
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun setProjection(proj: MapProjection) {
        _uiState.update { it.copy(projection = proj) }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying, isRealTime = false) }
    }

    fun setRealTimeSync(sync: Boolean) {
        _uiState.update {
            val now = System.currentTimeMillis()
            it.copy(
                isRealTime = sync,
                isPlaying = true,
                timeSpeedMultiplier = 1.0,
                currentTimestampMillis = if (sync) now else it.currentTimestampMillis
            )
        }
    }

    fun setTimeSpeed(multiplier: Double) {
        _uiState.update {
            it.copy(
                timeSpeedMultiplier = multiplier,
                isRealTime = false,
                isPlaying = true
            )
        }
    }

    fun setTimestamp(millis: Long) {
        _uiState.update {
            val updatedTelemetry = CelestialEngine.calculateState(millis, it.observerLocation)
            it.copy(
                currentTimestampMillis = millis,
                isRealTime = false,
                telemetry = updatedTelemetry
            )
        }
    }

    fun jumpToSeason(dayOfYear: Int) {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = _uiState.value.currentTimestampMillis
            set(java.util.Calendar.DAY_OF_YEAR, dayOfYear)
        }
        setTimestamp(cal.timeInMillis)
    }

    fun selectObserverLocation(city: CityLocation) {
        _uiState.update {
            val updatedTelemetry = CelestialEngine.calculateState(it.currentTimestampMillis, city)
            it.copy(
                observerLocation = city,
                telemetry = updatedTelemetry,
                showLocationPicker = false
            )
        }
    }

    fun setCustomObserverLocation(lat: Double, lon: Double) {
        val customCity = CityLocation(
            nameRu = "Координаты (${String.format("%.1f", lat)}°, ${String.format("%.1f", lon)}°)",
            nameEn = "Custom Location",
            latitude = lat.coerceIn(-90.0, 90.0),
            longitude = lon.let {
                var l = it % 360.0
                if (l < -180.0) l += 360.0
                if (l > 180.0) l -= 360.0
                l
            },
            description = "Пользовательская точка наблюдения на диске"
        )
        selectObserverLocation(customCity)
    }

    fun updateCamera3D(pitchDelta: Float, yawDelta: Float, zoomDelta: Float = 0f) {
        _uiState.update {
            val newPitch = (it.camera3D.pitchDeg + pitchDelta).coerceIn(5f, 85f)
            val newYaw = (it.camera3D.yawDeg + yawDelta) % 360f
            val newZoom = (it.camera3D.zoom * (1f + zoomDelta)).coerceIn(0.5f, 3.5f)
            it.copy(camera3D = it.camera3D.copy(pitchDeg = newPitch, yawDeg = newYaw, zoom = newZoom))
        }
    }

    fun updateObserverCamera(azimuthDelta: Float, elevationDelta: Float, zoomDelta: Float = 0f) {
        _uiState.update {
            var newAz = (it.observerCamera.azimuthHeadingDeg + azimuthDelta) % 360f
            if (newAz < 0) newAz += 360f
            val newEl = (it.observerCamera.elevationPitchDeg + elevationDelta).coerceIn(0f, 90f)
            val newZoom = (it.observerCamera.fovZoom * (1f + zoomDelta)).coerceIn(0.55f, 2.5f)
            it.copy(
                observerCamera = it.observerCamera.copy(
                    azimuthHeadingDeg = newAz,
                    elevationPitchDeg = newEl,
                    fovZoom = newZoom
                )
            )
        }
    }

    fun toggleLayer(update: LayerSettings.() -> LayerSettings) {
        _uiState.update { it.copy(layers = it.layers.update()) }
    }

    fun setShowTheoryDialog(show: Boolean) {
        _uiState.update { it.copy(showTheoryDialog = show) }
    }

    fun setShowLocationPicker(show: Boolean) {
        _uiState.update { it.copy(showLocationPicker = show) }
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }
}
