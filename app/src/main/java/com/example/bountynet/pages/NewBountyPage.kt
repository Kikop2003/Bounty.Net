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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBountyPage(
    navController: NavHostController,
) {
    var name by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("") }
    var selectedPlanet by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) } // Track the dialog's visibility
    var errorName by remember { mutableStateOf(false) } // Error message for validation
    var errorPlanet by remember { mutableStateOf(false) } // Error message for validation
    var errorReward by remember { mutableStateOf(false) } // Error message for validation

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
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorName = false // Reset error when user types
                },
                label = { Text("Name") },
                singleLine = true,
                isError = errorName, // Indicate error if empty
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
            if (errorName) {
                Text(
                    text = "Name cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = reward,
                onValueChange = {
                    reward = it
                    errorReward = false // Reset error when user types
                },
                label = { Text("Reward") },
                singleLine = true,
                isError = errorReward, // Indicate error if empty
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            if (errorReward) {
                Text(
                    text = "Reward must be a number",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // TextField for Planet selection
            TextField(
                value = selectedPlanet,
                onValueChange = { /* Disable manual edit */ },
                label = { Text("Planet") },
                singleLine = true,
                enabled = false, // Disable manual input
                readOnly = true, // Make it read-only as user will select planet from dialog
                modifier = Modifier.fillMaxWidth()
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

            // Open planet selection dialog if it's open
            if (isDialogOpen) {
                PlanetSelectionDialog(
                    onPlanetSelected = { planet ->
                        selectedPlanet = planet
                        isDialogOpen = false // Close the dialog after selecting a planet
                        errorPlanet = false
                    },
                    onDismissRequest = { isDialogOpen = false } // Close dialog if clicked outside
                )
            }

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
                        // Validate inputs
                        if (name.trim().isEmpty()) {
                            errorName = true
                        }
                        if (reward.trim().toDoubleOrNull() == null || reward.trim().toDoubleOrNull()!! <= 0) {
                            errorReward = true
                        }
                        if (selectedPlanet.isEmpty()) {
                            errorPlanet = true
                        }
                        if (!errorName && !errorReward && !errorPlanet) {
                            val bounty = Bounty(
                                name = name,
                                reward = reward.toDoubleOrNull() ?: 0.0,
                                planeta = selectedPlanet
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

                // Planet Buttons
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
