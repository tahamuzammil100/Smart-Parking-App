package com.example.smartparking.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartparking.model.ParkingLot
import com.example.smartparking.viewmodel.ParkingUiState
import com.example.smartparking.viewmodel.ParkingViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*
import com.example.smartparking.R

@Composable
fun MapScreen(viewModel: ParkingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ParkingUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ParkingUiState.Success -> {
                MapContent(parkingLots = state.parkingLots)
            }
            is ParkingUiState.Error -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connection Error",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MapContent(parkingLots: List<ParkingLot>) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val locationOverlay = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) }
    var isInitialized by remember { mutableStateOf(false) }

    AndroidView(
        factory = {
            mapView.apply {
                setMultiTouchControls(true)
                controller.setZoom(14.0)

                // Add user location overlay (blue dot)
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()
                overlays.add(locationOverlay)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { map ->
            if (parkingLots.isNotEmpty()) {
                updateMapMarkers(map, parkingLots, context, isInitialized, locationOverlay)
                if (!isInitialized) {
                    isInitialized = true
                }
            }
        }
    )

    DisposableEffect(Unit) {
        mapView.onResume()
        locationOverlay.enableMyLocation()
        onDispose {
            locationOverlay.disableMyLocation()
            mapView.onPause()
        }
    }
}

private fun updateMapMarkers(mapView: MapView, parkingLots: List<ParkingLot>, context: Context, isInitialized: Boolean, locationOverlay: MyLocationNewOverlay) {
    // Only clear overlays if we're updating, not on first load
    if (isInitialized) {
        // Just update existing markers instead of recreating
        val existingMarkers = mapView.overlays.filterIsInstance<Marker>()

        parkingLots.forEachIndexed { index, lot ->
            if (index < existingMarkers.size) {
                val marker = existingMarkers[index]
                val wasInfoWindowOpen = marker.isInfoWindowShown

                // Close info window before updating
                if (wasInfoWindowOpen) {
                    marker.closeInfoWindow()
                }

                marker.title = "${lot.name}\n${lot.freeSlots} free / ${lot.totalSlots} total"
                marker.snippet = "Last Updated: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}\nTap to navigate"

                // Update icon if status changed
                val iconRes = when {
                    lot.freeSlots == 0 -> R.drawable.parking_full
                    lot.freeSlots < lot.totalSlots * 0.3 -> R.drawable.parking_limited
                    else -> R.drawable.parking_available
                }
                marker.icon = context.getDrawable(iconRes)

                // Reopen info window with updated content
                if (wasInfoWindowOpen) {
                    marker.showInfoWindow()
                }
            }
        }
        mapView.invalidate()
        return
    }

    // First time initialization - keep location overlay, remove only markers
    mapView.overlays.removeAll { it is Marker }

    parkingLots.forEach { lot ->
        val position = GeoPoint(lot.latitude, lot.longitude)

        // Choose icon based on availability of parking spot
        val iconRes = when {
            lot.freeSlots == 0 -> R.drawable.parking_full
            lot.freeSlots < lot.totalSlots * 0.3 -> R.drawable.parking_limited
            else -> R.drawable.parking_available
        }

        val marker = Marker(mapView).apply {
            this.position = position
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = context.getDrawable(iconRes)
            title = "${lot.name}\n${lot.freeSlots} free / ${lot.totalSlots} total"
            snippet = "Last Updated: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}\nTap to navigate"

            setOnMarkerClickListener { clickedMarker, _ ->
                clickedMarker.showInfoWindow()
                openNavigation(context, lot.latitude, lot.longitude, lot.name)
                true
            }
        }
        mapView.overlays.add(marker)
        // Show info window on first load
        marker.showInfoWindow()
    }

    // Center map on first parking lot only - user can drag to see others
    if (parkingLots.isNotEmpty()) {
        val firstLot = parkingLots[0]
        mapView.controller.setCenter(GeoPoint(firstLot.latitude, firstLot.longitude))
        mapView.controller.setZoom(15.0)
    }

    mapView.invalidate()
}

private fun openNavigation(context: Context, lat: Double, lng: Double, name: String) {
    val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&label=${Uri.encode(name)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}
