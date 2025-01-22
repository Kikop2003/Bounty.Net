package com.example.bountynet.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.Bounty
import com.example.bountynet.Objects.User
import com.example.bountynet.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson

@Composable
fun Profile(modifier: Modifier = Modifier, userId: String, navHostController: NavHostController) {
    var user by remember { mutableStateOf(User()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Track the current profile picture index in a state so that it triggers recomposition
    var profilePictureIndex by remember { mutableStateOf(user.profilePictureIndex) }

    // Fetch user data
    FirebaseHelper.getObjectById(
        path = "users",
        id = userId,
        type = User::class.java,
        onSuccess = { userRet ->
            user = userRet
            profilePictureIndex = userRet.profilePictureIndex // Set initial profile picture index
            isLoading = false
        },
        onFailure = { error ->
            errorMessage = error
            isLoading = false
        }
    )

    // Dynamically update the profile picture whenever profilePictureIndex changes
    val profilePictureResId = when (profilePictureIndex) {
        1 -> R.drawable.profile_picture_1
        2 -> R.drawable.profile_picture_2
        3 -> R.drawable.profile_picture_3
        4 -> R.drawable.profile_picture_4
        else -> R.drawable.profile_picture_1 // Default image if index is invalid
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "An error occurred",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Credits: ${user.creds}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = profilePictureResId),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        // Update the profile picture index
                                        changeUserImage(
                                            userId,
                                            user,
                                            profilePictureIndex
                                        ) { newIndex ->
                                            profilePictureIndex = newIndex
                                        }
                                    }
                            )
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Profile Picture",
                                modifier = Modifier
                                    .size(35.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clickable {
                                        changeUserImage(
                                            userId,
                                            user,
                                            profilePictureIndex
                                        ) { newIndex ->
                                            profilePictureIndex = newIndex
                                        }
                                    },
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .clickable { // Make the entire row clickable
                                if (user.currentBountyId.isNotEmpty()) {
                                    FirebaseHelper.getObjectById(
                                        path = "bountys",
                                        id = user.currentBountyId,
                                        type = Bounty::class.java,
                                        onSuccess = { bounty ->
                                            navigateToBountyDetails(
                                                bountyPair = Pair(user.currentBountyId, bounty),
                                                navHostController = navHostController
                                            )
                                        },
                                        onFailure = { error ->
                                            Log.e("Profile", "Failed to fetch current bounty: $error")
                                        }
                                    )
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Bounty",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (user.currentBountyId.isNotEmpty()) {
                            var currentBounty by remember { mutableStateOf<Bounty?>(null) }

                            LaunchedEffect(user.currentBountyId) {
                                FirebaseHelper.getObjectById(
                                    path = "bountys",
                                    id = user.currentBountyId,
                                    type = Bounty::class.java,
                                    onSuccess = { bounty -> currentBounty = bounty },
                                    onFailure = { error -> currentBounty = null }
                                )
                            }

                            currentBounty?.let { bounty ->
                                Text(
                                    text = bounty.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } ?: Text(
                                text = "No Active Bounty",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Text(
                                text = "None",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }


                    ExpandableList(userId = userId, navHostController = navHostController)
                }
            }
        }
    }
}

fun changeUserImage(userId: String, user: User, currentProfilePictureIndex: Int, onProfilePictureUpdated: (Int) -> Unit) {
    // Increment the profile picture index, ensuring it's between 1 and 4
    val newProfilePictureIndex = (currentProfilePictureIndex % 4) + 1

    // Update the local object (if needed)
    user.profilePictureIndex = newProfilePictureIndex

    // Now update the Firebase database with the new profilePictureIndex
    FirebaseHelper.changeProfilePictureIndex(userId, newProfilePictureIndex,
        onSuccess = {
            // Successfully updated the profilePictureIndex in Firebase
            onProfilePictureUpdated(newProfilePictureIndex) // Notify the composable to update
            println("Profile picture index updated successfully in Firebase")
        },
        onFailure = { errorMessage ->
            // Handle failure to update the index
            println("Failed to update profile picture index: $errorMessage")
        }
    )
}

@Composable
fun ExpandableList(userId: String, navHostController: NavHostController) {
    var isExpanded by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<Pair<String, Bounty>>>(emptyList()) }

    LaunchedEffect(userId) {
        FirebaseHelper.getUserCreatedBountys(
            userId = userId,
            onSucess = { fetchedItems ->
                items = fetchedItems
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
//            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Created Bounties",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (isExpanded) "▲" else "▼", // Arrow direction changes based on isExpanded
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    if (isExpanded) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigateToBountyDetails(bountyPair = item, navHostController = navHostController)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (item.second.name.length > 17) "${item.second.name.take(17)}..." else item.second.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (item.second.concluida) TextDecoration.LineThrough else null
                        ),
                        color = if (item.second.concluida) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (item.second.concluida) "Concluded" else "In Progress...",
                        style = MaterialTheme.typography.bodySmall.copy(),
                        color = if (item.second.concluida) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


fun navigateToBountyDetails(bountyPair: Pair<String, Bounty>, navHostController: NavHostController) {
    val gson = Gson()
    val bountyJson = gson.toJson(bountyPair)
    navHostController.navigate("bountyDetail/$bountyJson")
}
