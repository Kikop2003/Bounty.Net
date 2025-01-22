package com.example.bountynet.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
    // State for loading
    var isLoading by remember { mutableStateOf(true) }
    // State for errors
    var errorMessage by remember { mutableStateOf<String?>(null) }

    FirebaseHelper.getObjectById(
        path = "users",
        id = userId,
        type = User::class.java,
        onSuccess = { userRet ->
            user = userRet
            isLoading = false
        },
        onFailure = { error ->
            errorMessage = error
            isLoading = false
        }
    )

    Box(
        modifier = modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
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
                        Image(
                            painter = painterResource(id = R.drawable.profile_picture_1),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(200.dp)
                                .graphicsLayer(shape = CircleShape, clip = true)
                                .clickable {
                                    changeUserImage()
                                }
                        )

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
                            .padding(start = 16.dp),
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
                                    onSuccess = { bounty ->
                                        currentBounty = bounty
                                    },
                                    onFailure = { error ->
                                        currentBounty = null // Handle error appropriately
                                    }
                                )
                            }

                            currentBounty?.let { bounty ->
                                Text(
                                    text = bounty.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.clickable {
                                        navigateToBountyDetails(bountyPair = Pair(user.currentBountyId, bounty), navHostController = navHostController)
                                    }
                                )
                            } ?: Text(
                                text = "Loading...",
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

fun changeUserImage() {
    TODO("Not yet implemented")

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
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = if (item.second.concluida) TextDecoration.LineThrough else null
                        ),
                        color = if (item.second.concluida) Color.Gray else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
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
                .height(200.dp)
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

    db.collection("bounties").document(bountyId)
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

fun navigateToBountyDetails(bountyPair: Pair<String, Bounty>, navHostController: NavHostController) {
    val gson = Gson()
    val bountyJson = gson.toJson(bountyPair)
    navHostController.navigate("bountyDetail/$bountyJson")
}
