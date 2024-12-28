package com.example.bountynet.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.runtime.getValue
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.Bounty
import com.google.android.gms.location.*
import com.google.maps.android.compose.*


@Composable
fun Current(modifier: Modifier = Modifier, userId: String){
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    // Automatically check and request permission
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    var bounty by remember { mutableStateOf<Bounty?>(null) }
    var failure by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) } // State for loading

    // Fetch bounty information
    LaunchedEffect(userId) {
        isLoading = true
        FirebaseHelper.getUserBounty(
            userId = userId,
            onSuccess = { ret ->
                bounty = ret
                failure = false
                isLoading = false
            },
            onFailure = { ret ->
                text = ret
                failure = true
                isLoading = false
            }
        )
    }

    when {
        isLoading -> {
            // Circular Progress Indicator for loading state
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        bounty != null -> {
            if (permissionGranted) {
                GoogleMapsScreen(modifier)
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Permission for fine Location not granted",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        failure -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = text,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

}


@Composable
fun GoogleMapsScreen(modifier: Modifier) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Set up CameraPositionState
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f) // Initial position and zoom
    }

    val userLocation = remember { mutableStateOf<LatLng?>(null) }
    val destination = LatLng(37.7749, -122.4194) // Example destination
    var startLocation = remember { mutableStateOf<LatLng?>(null) }
    // Get the user's current location
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    userLocation.value = latLng
                    startLocation.value = latLng
                    // Move the camera to the user's current location
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 10f)
                }
            }
        }
    }
    // Real-time location updates
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 5000)
        .setMinUpdateIntervalMillis(2000)
        .build()


    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                userLocation.value = latLng
                // Update camera position only if needed
            }
        }
    }
    DisposableEffect(context) {
        // Request location updates
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        onDispose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    // Render the Google Map with the camera position state
    Box() {
        GoogleMap(
            modifier = Modifier.fillMaxSize(), // para nao ter a barra de navegação a sobrepor ao mapa substituir por modifier
            cameraPositionState = cameraPositionState
        ) {
            userLocation.value?.let {
                // Display a marker at the user's current location
                Marker(
                    state = MarkerState(position = it),
                    title = "You are here"
                )
            }
            startLocation.value?.let {
                // Display a marker at the user's current location
                Marker(
                    state = MarkerState(position = it),
                    title = "Start Location"
                )
            }
            Marker(
                state = rememberMarkerState(position = destination),
                title = "Destination"
            )
        }
    }
}