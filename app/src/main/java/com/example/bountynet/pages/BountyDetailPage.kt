package com.example.bountynet.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.Bounty
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun BountyDetailPage(pair: Pair<String, Bounty>, navController: NavHostController, userId: String) {
    val bounty = pair.second
    val id = pair.first

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Title
            Text(
                text = "Bounty Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(label = "Name", value = bounty.name, valueColor = MaterialTheme.colorScheme.secondary)
                DetailRow(label = "Reward", value = "$${bounty.reward}", valueColor = MaterialTheme.colorScheme.tertiary)

                // Planet Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Planet",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bounty.planeta,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        PlanetImage(bounty)
                    }
                }

                if (bounty.concluida) {
                    DetailRow(label = "Concluded", value = "Yes", valueColor = MaterialTheme.colorScheme.tertiary)
                    DetailRow(label = "Hunter", value = bounty.hunter, valueColor = MaterialTheme.colorScheme.secondary)

                    // Target's Photo Section
                    Text(
                        text = "Target's Photo", // Subtitle
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        textAlign = TextAlign.Center // Center the subtitle
                    )

                    // Display Target's Image if bounty is concluded
                    DisplayBountyPhoto(bountyId = id)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = "Go Back",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                if (bounty.hunter == "None") {
                    Button(
                        onClick = {
                        FirebaseHelper.acceptBounty(id, userId, callback = { text ->
                            Toast.makeText(navController.context, text, Toast.LENGTH_SHORT).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(
                            text ="Accept",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                } else if (bounty.hunter == userId && !bounty.concluida) {
                    Button(onClick = {
                        FirebaseHelper.releaseBounty(id, userId, callback = { text ->
                            Toast.makeText(navController.context, text, Toast.LENGTH_SHORT).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(
                            text = "Give Up",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DetailRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
fun DisplayBountyPhoto(bountyId: String) {
    var photoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bountyId) {
        getPhotoUrlFromDatabase(bountyId, onSuccess = { url ->
            photoUrl = url
        }, onFailure = { e ->
            // Handle error
        })
    }

    if (photoUrl != null) {
        val painter = rememberAsyncImagePainter(photoUrl)
        Image(
            painter = painter,
            contentDescription = "Bounty Photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Set a larger height for the image
                .padding(16.dp)
        )
    } else {
        Text(
            text = "Loading photo...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getPhotoUrlFromDatabase(bountyId: String, onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
    val db = Firebase.firestore

    db.collection("bountys").document(bountyId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val photoUrl = document.getString("photoUrl")
                onSuccess(photoUrl)
            } else {
                onSuccess(null)
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}
