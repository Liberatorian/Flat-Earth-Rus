package com.example.ui

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.FlatEarthViewModel
import com.example.model.ViewMode

@Composable
fun FlatEarthCosmosApp(
    viewModel: FlatEarthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
        AmbientAudioEffect(enabled = state.isAmbientAudioEnabled)
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
                        onTapLocation = { lat, lon -> viewModel.setCustomObserverLocation(lat, lon) }
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
