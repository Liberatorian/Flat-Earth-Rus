package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.model.FlatEarthViewModel
import com.example.model.ViewMode

@Composable
fun FlatEarthCosmosApp(
    viewModel: FlatEarthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var locationStatus by remember { mutableStateOf<String?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setCustomObserverLocation(location.latitude, location.longitude)
                    viewModel.setShowLocationPicker(false)
                    locationStatus = null
                } else {
                    locationStatus = "Позиция пока недоступна. Включите геолокацию и повторите попытку."
                    viewModel.setShowLocationPicker(true)
                }
            }
        } else {
            locationStatus = "Доступ к геопозиции не предоставлен. Координаты можно ввести вручную."
            viewModel.setShowLocationPicker(true)
        }
    }
    val requestCurrentLocation: () -> Unit = {
        locationStatus = "Запрашиваю текущую геопозицию..."
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setCustomObserverLocation(location.latitude, location.longitude)
                    viewModel.setShowLocationPicker(false)
                    locationStatus = null
                } else {
                    locationStatus = "Позиция пока недоступна. Включите геолокацию и повторите попытку."
                    viewModel.setShowLocationPicker(true)
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    LaunchedEffect(Unit) {
        requestCurrentLocation()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            CosmosTopBar(
                state = state,
                onViewModeChange = { viewModel.setViewMode(it) },
                onOpenLocationPicker = { viewModel.setShowLocationPicker(true) },
                onOpenLayers = { viewModel.setShowSettingsSheet(true) },
                onOpenTheory = { viewModel.setShowTheoryDialog(true) }
                    ,onOpenReference = { viewModel.setShowReferenceDialog(true) }
                    ,onToggleAmbientAudio = { viewModel.setAmbientAudioEnabled(!state.isAmbientAudioEnabled) }
                    ,onAmbientVolumeChange = viewModel::setAmbientAudioVolume
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TelemetryDetailCard(
                    telemetry = state.telemetry,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                TelemetryAndControlBar(
                    state = state,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onRealTimeSync = { viewModel.setRealTimeSync(it) },
                    onSetSpeed = { viewModel.setTimeSpeed(it) },
                    onJumpSeason = { viewModel.jumpToSeason(it) }
                )
            }
        },
        containerColor = Color(0xFF030712)
    ) { innerPadding ->
        AmbientAudioEffect(
            enabled = state.isAmbientAudioEnabled,
            volume = state.ambientAudioVolume
        )
        CompassSensorEffect(
            enabled = state.isCompassModeEnabled && state.viewMode == ViewMode.OBSERVER_SKY,
            onOrientationChanged = viewModel::setSensorOrientation
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.viewMode) {
                ViewMode.DOME_3D -> {
                    Dome3DView(
                        state = state,
                        onCameraDelta = { pitch, yaw -> viewModel.updateCamera3D(pitch, yaw) }
                    )
                }

                ViewMode.OBSERVER_SKY -> {
                    ObserverSkyView(
                        state = state,
                        onCameraDelta = { az, el, zoom -> viewModel.updateObserverCamera(az, el, zoom) },
                        onToggleCompass = { viewModel.setCompassModeEnabled(!state.isCompassModeEnabled) },
                        onToggleHud = { viewModel.setObserverHudVisible(!state.showObserverHud) },
                        onSetCamera = { az, el ->
                            val deltaAz = az - state.observerCamera.azimuthHeadingDeg
                            val deltaEl = el - state.observerCamera.elevationPitchDeg
                            viewModel.updateObserverCamera(deltaAz, deltaEl)
                        }
                    )
                }

                ViewMode.GLEASON_2D -> {
                    Gleason2DView(
                        state = state,
                        onTapLocation = { lat, lon -> viewModel.setCustomObserverLocation(lat, lon) },
                        onZoomIn = { viewModel.updateMapZoom(0.25f) },
                        onZoomOut = { viewModel.updateMapZoom(-0.25f) },
                        onResetZoom = viewModel::resetMapZoom,
                        onTransform = viewModel::updateMapTransform
                    )
                }
            }

            // Location Picker Dialog
            if (state.showLocationPicker) {
                LocationPickerDialog(
                    currentCity = state.observerLocation,
                    onSelectCity = { viewModel.selectObserverLocation(it) },
                    onCustomLocation = { lat, lon ->
                        viewModel.setCustomObserverLocation(lat, lon)
                        viewModel.setShowLocationPicker(false)
                    },
                    onRequestCurrentLocation = requestCurrentLocation,
                    locationStatus = locationStatus,
                    onDismiss = { viewModel.setShowLocationPicker(false) }
                )
            }

            // Theory and Physics Dialog
            if (state.showTheoryDialog) {
                TheoryDialog(
                    onDismiss = { viewModel.setShowTheoryDialog(false) }
                )
            }

            if (state.showReferenceDialog) {
                ModelReferenceDialog(
                    onDismiss = { viewModel.setShowReferenceDialog(false) }
                )
            }

            // Layer Settings Bottom Sheet
            if (state.showSettingsSheet) {
                LayersBottomSheet(
                    layers = state.layers,
                    projection = state.projection,
                    onToggleLayer = { viewModel.toggleLayer(it) },
                    onProjectionChange = { viewModel.setProjection(it) },
                    onDismiss = { viewModel.setShowSettingsSheet(false) }
                )
            }
        }
    }
}
