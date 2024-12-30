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
import android.location.Location.distanceBetween
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.Bounty
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*


@Composable
fun Current(
    modifier: Modifier = Modifier,
    userId: String,
    navHostController: NavHostController
){
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
                GoogleMapsScreen(modifier,navHostController, bounty!!, userId)
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
fun GoogleMapsScreen(
    modifier: Modifier,
    navController: NavHostController,
    bounty: Bounty,
    userId: String
) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    // Set up CameraPositionState
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f) // Initial position and zoom
    }

    val userLocation = remember { mutableStateOf<LatLng?>(null) }
    val destination = LatLng(bounty.lat, bounty.lon) // Example destination

    val currentBackStackEntry = navController.currentBackStackEntry
    val savedLocal = currentBackStackEntry?.savedStateHandle?.get<LatLng?>("startLocation")

    var startLocation = remember { mutableStateOf<LatLng?>(savedLocal)}


    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d("GoogleMapsScreen", "Location: ${it.latitude}, ${it.longitude}")
                    val latLng = LatLng(it.latitude, it.longitude)
                    userLocation.value = latLng
                    if (startLocation.value == null){
                        navController.currentBackStackEntry?.savedStateHandle?.set("startLocation", latLng)
                        startLocation.value = latLng
                    }
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                } ?: Toast.makeText(context, "Remember To turn on location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Real-time location updates
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 5000)
        .setMinUpdateIntervalMillis(2000)
        .build()

    var showButton by remember { mutableStateOf(false) }
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                userLocation.value = latLng
                // Update camera position only if needed
                if (startLocation.value == null){
                    navController.currentBackStackEntry?.savedStateHandle?.set("startLocation", latLng)
                    startLocation.value = latLng
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                }
                val results = FloatArray(1)
                distanceBetween(location.latitude, location.longitude, destination.latitude, destination.longitude, results)
                showButton = results[0] <= 100
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


    // Check if the user is within 10 meters of the destination



    // Render the Google Map with the camera position state
    Box {
        Box {
            GoogleMap(
                modifier = Modifier.fillMaxSize(), // Replace with your specific modifier if needed
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
                    // Display a marker at the start location
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

            if (showButton) {
                Button(
                    onClick = { FirebaseHelper.concludeBounty(userId)},
                    modifier = Modifier
                        .padding(bottom = 128.dp) // Padding to adjust the button vertically
                        .align(Alignment.BottomCenter) // Align the button at the bottom
                ) {
                    Text("Conclude Bounty")
                }
            }
        }
    }
}
