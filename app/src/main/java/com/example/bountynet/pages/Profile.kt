package com.example.bountynet.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.User
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

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
                }
            }
        }
    }
}

