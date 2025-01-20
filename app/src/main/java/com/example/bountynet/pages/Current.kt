package com.example.bountynet.pages


import android.Manifest
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.Location.distanceBetween
import android.os.Build
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavHostController
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.Bounty
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.example.bountynet.R

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
            Box(modifier = modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = modifier.align(Alignment.Center)
                )
            }
        }
        bounty != null -> {
            if (permissionGranted) {
                GoogleMapsScreen(modifier,navHostController, bounty!!, userId)
            } else {
                Box(modifier = modifier.fillMaxSize()) {
                    Text(
                        text = "Permission for fine Location not granted",
                        modifier = modifier.align(Alignment.Center)
                    )
                }
            }
        }
        failure -> {
            Box(modifier = modifier.fillMaxSize()) {
                Text(
                    text = text,
                    modifier = modifier.align(Alignment.Center)
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

    var results by remember { mutableFloatStateOf((0f)) }
    var showButton by remember { mutableStateOf(false) }

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
                    if (bounty.planeta == "Earth"){
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                    }
                    var resu = FloatArray(1)
                    distanceBetween(location.latitude, location.longitude, destination.latitude, destination.longitude, resu)
                    results = resu[0]
                    showButton = resu[0] <= 100
                } ?: Toast.makeText(context, "Remember To turn on location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Real-time location updates
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setMinUpdateIntervalMillis(2000)
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                userLocation.value = latLng
                // Update camera position only if needed
                if (startLocation.value == null && bounty.planeta == "Earth"){
                    navController.currentBackStackEntry?.savedStateHandle?.set("startLocation", latLng)
                    startLocation.value = latLng
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                }
                var resu = FloatArray(1)
                distanceBetween(location.latitude, location.longitude, destination.latitude, destination.longitude, resu)
                results = resu[0]
                showButton = resu[0] <= 100
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

    Box {
        Box {
            if (bounty.planeta == "Earth") {
                GoogleMap(
                    modifier = modifier.fillMaxSize(), // Replace with your specific modifier if needed
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
            }else{
                val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
                ArrowScreen(
                    currentLat = userLocation.value?.latitude ?: 0.0,
                    currentLng = userLocation.value?.longitude ?: 0.0,
                    destinationLat = destination.latitude,
                    destinationLng = destination.longitude,
                    sensorManager = sensorManager
                )
            }
            Text(
                modifier = Modifier
                    .padding(bottom = 180.dp) // Padding to adjust the button vertically
                    .align(Alignment.BottomCenter)
                    .background(Color.Black) // Set the background color to black
                    .padding(8.dp), // Add some padding around the text to make it more readable
                text = adapt(results),
                color = Color.White, // Set the text color to white to contrast against the black background
                fontWeight = FontWeight.Bold // Make the text bold
            )
            if (showButton) {
                vibratePhone(context)
                Button(
                    onClick = {
                        navController.navigate("photo")
                              },
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

fun adapt(distance: Float): String {
    return if(distance < 1000){
        "%.0f km".format(distance/1000)
    }else{
        "%.1f km".format(distance/1000)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun vibratePhone(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(100)
    }
}

@Composable
fun ArrowScreen(
    currentLat: Double,
    currentLng: Double,
    destinationLat: Double,
    destinationLng: Double,
    sensorManager: SensorManager
) {
    val scope = rememberCoroutineScope()

    // State variables for orientation and direction
    var azimuth by remember { mutableFloatStateOf(0f) }
    var direction by remember { mutableFloatStateOf(0f) }

    // Calculate bearing
    val bearing = calculateBearing(currentLat, currentLng, destinationLat, destinationLng)

    // Sensor listener
    val sensorListener = remember {
        object : SensorEventListener {
            private var gravity: FloatArray? = null
            private var geomagnetic: FloatArray? = null

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> gravity = event.values
                    Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
                }

                if (gravity != null && geomagnetic != null) {
                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    }
                }
                // Update the direction state
                scope.launch(Dispatchers.Main) {
                    direction = (bearing - azimuth + 360) % 360
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Register sensors
    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    // Display the arrow
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.arrow),
            contentDescription = "Arrow pointing to destination",
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer { rotationZ = direction }
        )

    }
}

// Function to calculate bearing
fun calculateBearing(
    currentLat: Double,
    currentLng: Double,
    destinationLat: Double,
    destinationLng: Double
): Float {
    val deltaLng = Math.toRadians(destinationLng - currentLng)
    val currentLatRad = Math.toRadians(currentLat)
    val destinationLatRad = Math.toRadians(destinationLat)

    val y = sin(deltaLng) * cos(destinationLatRad)
    val x = cos(currentLatRad) * sin(destinationLatRad) -
            sin(currentLatRad) * cos(destinationLatRad) * cos(deltaLng)

    val bearing = Math.toDegrees(atan2(y, x))
    return ((bearing + 360) % 360).toFloat()
}

