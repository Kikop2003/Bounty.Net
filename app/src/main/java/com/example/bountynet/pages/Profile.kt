package com.example.bountynet.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.User
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.bountynet.Objects.Bounty
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun Profile(modifier: Modifier = Modifier, userId: String, ) {
    // State for user object
    var user by remember { mutableStateOf(User()) }
    // State for loading
    var isLoading by remember { mutableStateOf(true) }
    // State for errors
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Simulate fetching user data from Firebase (replace with your FirebaseHelper)
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
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "An error occurred",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                // UI: Loading indicator, error message, or user profile

                // Show the user profile information
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(text = "User Profile")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Username: ${user.username}")
                    Text(text = "Completed Bounties: ${user.completedBounties}")
                    Text(text = "Average Time: ${user.averageTime}")
                    Text(text = "Credits: ${user.creds}")
                    Text(text = "Current Bounty ID: ${user.currentBountyId}")
                    ExpandableList(userId)
                }
            }
        }
    }
}

@Composable
fun ExpandableList(userId: String) {
    // State for the expandable list
    var isExpanded by remember { mutableStateOf(false) }

    // Use mutableStateOf to hold the items and trigger recomposition
    var items by remember { mutableStateOf<List<Pair<String, Bounty>>>(emptyList()) }

    // Fetch the items when the composable is first launched
    LaunchedEffect(userId) {
        FirebaseHelper.getUserCreatedBountys(
            userId = userId,
            onSucess = { fetchedItems ->
                items = fetchedItems
            }
        )
    }

    // Header for the expandable list
    Text(
        text = "Expandable List", // Title for the expandable list
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { isExpanded = !isExpanded } // Toggle expand/collapse
            .background(if (isExpanded) Color.LightGray else Color.White)
            .padding(16.dp),
        color = Color.Black
    )

    // The expandable list
    if (isExpanded) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp) // Limit height for large lists
                .background(Color(0xFFF7F7F7))
        ) {
            items(items) { item ->
                if (item.second.concluida) {
                    Text(
                        text = "${item.second.name} ----- Concluida",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DisplayBountyPhoto(item.first)
                } else {
                    Text(
                        text = "${item.second.name} ----- Nao Concluida",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color.Black
                    )
                }
            }
        }
    }
}


// Composable function to display the photo
@Composable
fun DisplayBountyPhoto(bountyId: String) {
    var photoUrl by remember { mutableStateOf<String?>(null) }

    // Fetch the photo URL when the composable is first launched
    LaunchedEffect(bountyId) {
        getPhotoUrlFromDatabase(bountyId, onSuccess = { url ->
            photoUrl = url
        }, onFailure = { e ->
            Log.e("Firestore", "Error fetching photo URL", e)
        })
    }

    // Show a placeholder image while the photo URL is being fetched
    if (photoUrl != null) {
        // Load the image using Coil if the photoUrl is available
        val painter = rememberAsyncImagePainter(photoUrl)
        Image(
            painter = painter,
            contentDescription = "Bounty Photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjust the height as needed
                .padding(16.dp)
        )
    } else {
        // Show a placeholder or loading text while the URL is being fetched
        Text(
            text = "Loading photo...",
        )
    }
}



fun getPhotoUrlFromDatabase(bountyId: String, onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
    val db = Firebase.firestore

    // Get the document for the given bountyId
    db.collection("bounties").document(bountyId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val photoUrl = document.getString("photoUrl")
                onSuccess(photoUrl)
            } else {
                Log.e("Firestore", "No document found with bountyId: $bountyId")
                onSuccess(null)
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error getting document", e)
            onFailure(e)
        }
}


