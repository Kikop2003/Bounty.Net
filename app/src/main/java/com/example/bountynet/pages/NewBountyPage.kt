package com.example.bountynet.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.bountynet.Objects.Bounty
import com.example.bountynet.FirebaseHelper
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun CreateBountyPage(
    navController: NavHostController,
    userId : String
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Use theme background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Create Bounty",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            var name by remember { mutableStateOf("") }
            var errorName by remember { mutableStateOf(false) }

            ValidatedInput(
                value = name,
                label = "Name",
                errorText = "Name cannot be empty",
                isError = errorName,
                onValueChange = {
                    name = it
                    errorName = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            var reward by remember { mutableStateOf("") }
            var errorReward by remember { mutableStateOf(false) } // Error message for validation
            ValidatedInput(
                value = reward,
                label = "Reward",
                errorText = "Reward must be a number",
                isError = errorReward,
                onValueChange = {
                    reward = it
                    errorReward = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            var latitude by remember { mutableStateOf("") }
            var longitude by remember { mutableStateOf("") }
            var errorLat by remember { mutableStateOf(false) }
            var errorLon by remember { mutableStateOf(false) }

            LatLongInput(
                latitude = latitude,
                longitude = longitude,
                errorLat = errorLat,
                errorLon = errorLon,
                onLatitudeChange = {
                    latitude = it
                    errorLat = false
                },
                onLongitudeChange = {
                    longitude = it
                    errorLon = false
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            var selectedPlanet by remember { mutableStateOf("") }
            var errorPlanet by remember { mutableStateOf(false) } // Error message for validation

            PlanetPicker(
                selectedPlanet = selectedPlanet,
                errorPlanet = errorPlanet,
                onPlanetSelected = { planet ->
                    selectedPlanet = planet
                    errorPlanet = false
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Confirm and Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cancel Button
                TextButton(
                    onClick = { navController.popBackStack() } // Navigate back
                ) {
                    Text("Cancel")
                }

                // Create Button
                TextButton(
                    onClick = {

                        val trimmedName = name.trim()

                        val rewardValue = reward.toDoubleOrNull()
                        val latitudeValue = latitude.toDoubleOrNull()
                        val longitudeValue = longitude.toDoubleOrNull()

                        errorName = trimmedName.isEmpty()
                        errorReward = rewardValue == null || rewardValue <= 0
                        errorPlanet = selectedPlanet.isEmpty()
                        errorLat = latitudeValue == null || latitudeValue < -90 || latitudeValue > 90
                        errorLon = longitudeValue == null || longitudeValue < -180 || longitudeValue > 180

                        if (!errorName && !errorReward && !errorPlanet && !errorLat && !errorLon) {
                            val bounty = Bounty(
                                name = trimmedName,
                                reward = rewardValue!!,
                                planeta = selectedPlanet,
                                createdBy = userId,
                                lat = latitudeValue!!,
                                lon = longitudeValue!!
                            )
                            FirebaseHelper.addToDatabase(
                                path = "bountys",
                                item = bounty,
                                onSuccess = { navController.popBackStack() },
                                onFailure = { error -> /* Handle error */ }
                            )
                        }
                    }
                ) {
                    Text("Create")
                }
            }
        }
    }
}

@Composable
fun ValidatedInput(
    value: String,
    label: String,
    errorText: String,
    isError: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                onValueChange(input) // Handle input changes
            },
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            colors = colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        if (isError) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}

@Composable
fun LatLongInput(
    latitude: String,
    longitude: String,
    errorLat: Boolean,
    errorLon: Boolean,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            OutlinedTextField(
                value = latitude,
                onValueChange = { input -> onLatitudeChange(input) },
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            if (errorLat) {
                Text(
                    text = "Please enter a valid latitude [-90,90]",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start) // Align within Column
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = longitude,
                onValueChange = { input -> onLongitudeChange(input) },
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            if (errorLon) {
                Text(
                    text = "Please enter a valid longitude [-180,180]",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start) // Align within Column
                )
            }
        }
    }
}

@Composable
fun PlanetPicker(
    selectedPlanet: String,
    errorPlanet: Boolean,
    onPlanetSelected: (String) -> Unit,
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = selectedPlanet,
            onValueChange = { /* Disable manual edit */ },
            label = { Text("Planet") },
            singleLine = true,
            enabled = false, // Disable manual input
            readOnly = true, // Make it read-only as user will select a planet from dialog
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDialogOpen = true } // Open dialog on click
        )
        if (errorPlanet) {
            Text(
                text = "Please select a planet",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        if (isDialogOpen) {
            PlanetSelectionDialog(
                onPlanetSelected = { planet ->
                    isDialogOpen = false
                    onPlanetSelected(planet)
                },
                onDismissRequest = { isDialogOpen = false }
            )
        }
    }
}

@Composable
fun PlanetSelectionDialog(
    onPlanetSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var possiblePlanets = Bounty.possiblePlanets
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select a Planet",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(possiblePlanets) { planet ->
                        Button(
                            onClick = {
                                onPlanetSelected(planet)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = planet)
                        }
                    }
                }
            }
        }
    }
}