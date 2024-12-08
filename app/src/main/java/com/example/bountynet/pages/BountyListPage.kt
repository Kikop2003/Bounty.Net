package com.example.bountynet.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bountynet.Bounty
import com.example.bountynet.FirebaseHelper
import com.google.gson.Gson

@Composable
fun BountyListPage(modifier: Modifier = Modifier, navHostController: NavHostController) {
    var isLoading by remember { mutableStateOf(true) }
    var items by remember { mutableStateOf<List<Bounty>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var isSortDialogOpen by remember { mutableStateOf(false) }
    var isFilterDialogOpen by remember { mutableStateOf(false) }
    var sortAscending by remember { mutableStateOf(true) }
    var sortProperty by remember { mutableStateOf("name") }
    var filterList by remember { mutableStateOf<List<String>>(Bounty.possiblePlanets) }

    // Load items from Firebase
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

    val filteredAndSortedItems = remember(items, searchText, sortAscending, sortProperty, filterList) {
        val filtered = if (searchText.isNotEmpty()) {
            items.filter {
                it.name.contains(searchText, ignoreCase = true) && filterList.contains(it.planeta)
            }
        } else {
            items.filter { filterList.contains(it.planeta) }
        }

        if (sortProperty == "name") {
            if (sortAscending) filtered.sortedBy { it.name.lowercase() } else filtered.sortedByDescending { it.name.lowercase() }
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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Filter and Sort Options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isSortDialogOpen = true }) {
                            Text("Sort By ${sortProperty.replaceFirstChar { it.uppercase() }}")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { isFilterDialogOpen = true }) {
                            Text("Filter by Planet")
                        }
                        TextButton(onClick = { sortAscending = !sortAscending }) {
                            Text(if (sortAscending) "Ascending" else "Descending")
                        }
                    }

                    // LazyColumn for items
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Show dialogs
                if (isSortDialogOpen) {
                    SortDialog(
                        currentSortProperty = sortProperty,
                        currentSortAscending = sortAscending,
                        onDismiss = { isSortDialogOpen = false },
                        onApply = { property, ascending ->
                            sortProperty = property
                            sortAscending = ascending
                        }
                    )
                }

                if (isFilterDialogOpen) {
                    FilterDialog(
                        filterList = filterList,
                        onFilterChange = { updatedList -> filterList = updatedList.toMutableList() },
                        onDismiss = { isFilterDialogOpen = false }
                    )
                }
            }
        }
    }
}

@Composable
fun SortDialog(
    currentSortProperty: String,
    currentSortAscending: Boolean,
    onDismiss: () -> Unit,
    onApply: (String, Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text("Sort Options") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onApply("name", currentSortAscending) }
                ) {
                    RadioButton(
                        selected = currentSortProperty == "name",
                        onClick = { onApply("name", currentSortAscending) }
                    )
                    Text("Sort by Name")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onApply("reward", currentSortAscending) }
                ) {
                    RadioButton(
                        selected = currentSortProperty == "reward",
                        onClick = { onApply("reward", currentSortAscending) }
                    )
                    Text("Sort by Reward")
                }
            }
        }
    )
}

@Composable
fun FilterDialog(
    filterList: List<String>,
    onFilterChange: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Apply")
            }
        },
        title = { Text("Filter by Planet") },
        text = {
            LazyColumn {
                items(Bounty.possiblePlanets) { planet ->
                    val isSelected = filterList.contains(planet)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                val updatedList = if (isSelected) {
                                    filterList.toMutableList().apply { remove(planet) }
                                } else {
                                    filterList.toMutableList().apply { add(planet) }
                                }
                                onFilterChange(updatedList)
                            }
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val updatedList = if (checked) {
                                    filterList.toMutableList().apply { add(planet) }
                                } else {
                                    filterList.toMutableList().apply { remove(planet) }
                                }
                                onFilterChange(updatedList)
                            }
                        )
                        Text(
                            text = planet,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    )
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
            .clickable { onClick(bounty) },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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
