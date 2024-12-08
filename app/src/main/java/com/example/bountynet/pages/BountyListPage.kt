package com.example.bountynet.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bountynet.FirebaseHelper
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.bountynet.Bounty
import com.google.gson.Gson


@Composable
fun BountyListPage(modifier: Modifier = Modifier, navHostController: NavHostController) {
    var isLoading by remember { mutableStateOf(true) }
    var items by remember { mutableStateOf<List<Bounty>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var isSortDialogOpen by remember { mutableStateOf(false) }
    var sortAscending by remember { mutableStateOf(true) }
    var sortProperty by remember { mutableStateOf("name") } // Default sort property

    LaunchedEffect(Unit) {
        FirebaseHelper.retrieveList(
            path = "bountys",
            type = Bounty::class.java,
            onSuccess = { retrievedItems ->
                items = retrievedItems
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    val filteredAndSortedItems = remember(items, searchText, sortAscending, sortProperty) {
        val filtered = if (searchText.isNotEmpty()) {
            items.filter { it.name.contains(searchText, ignoreCase = true) == true }
        } else {
            items
        }

        if (sortProperty == "name") {
            if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
        } else {
            if (sortAscending) filtered.sortedBy { it.reward } else filtered.sortedByDescending { it.reward }
        }
    }

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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search Bar
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search bounties...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        colors = colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Sort Options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isSortDialogOpen = true }) {
                            Text("Sort By ${sortProperty.replaceFirstChar { char -> char.uppercase() }}")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { sortAscending = !sortAscending }) {
                            Text(if (sortAscending) "Ascending" else "Descending")
                        }
                    }

                    // LazyColumn for items
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAndSortedItems) { bounty ->
                            BountyItem(
                                bounty = bounty,
                                onClick = { clickedItem ->
                                    val gson = Gson()
                                    val bountyJson = gson.toJson(bounty)

                                    navHostController.navigate("bountyDetail/$bountyJson")
                                }
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { navHostController.navigate("createBounty") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineLarge, // Use a larger predefined text style
                        color = MaterialTheme.colorScheme.onPrimary // Ensure color matches the FAB content
                    )
                }
            }
        }
    }
}




@Composable
    fun BountyItem(
        bounty: Bounty,
        onClick: (Bounty) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 0.dp)
                .clickable { onClick(bounty) }, // Handle click
            elevation = androidx.compose.material3.CardDefaults.cardElevation(8.dp), // Higher elevation for "pop-out" effect
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp) // Rounded corners
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Text(
                    text = bounty.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reward: ${bounty.reward} coins",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }





