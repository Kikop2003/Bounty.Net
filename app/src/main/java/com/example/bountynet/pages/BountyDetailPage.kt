package com.example.bountynet.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.Bounty

@Composable
fun BountyDetailPage(pair: Pair<String, Bounty>, navController: NavHostController,userId: String) {
    val bounty = pair.second
    val id = pair.first
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Bounty Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            DetailRow(label = "Name", value = bounty.name)
            DetailRow(label = "Reward", value = "$${bounty.reward}")
            DetailRow(label = "Planet", value = bounty.planeta)
            DetailRow(label = "Assigned Hunter", value = bounty.hunter)
            if (bounty.concluida) {
                DetailRow(label = "Concluded", value = "Yes")
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                Button(onClick = {
                    navController.popBackStack()
                }) {
                    Text("Go Back")
                }
                if (bounty.hunter == "None") {
                    Button(onClick = {
                        FirebaseHelper.acceptBounty(id, userId, callback = { text ->
                            Toast.makeText(navController.context, text, Toast.LENGTH_SHORT).show()
                        })
                    }) {
                        Text("Accept")
                    }
                } else if (bounty.hunter == userId) {
                    Button(onClick = {
                        FirebaseHelper.releaseBounty(id, userId, callback = { text ->
                            Toast.makeText(navController.context, text, Toast.LENGTH_SHORT).show()
                        })
                    }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}


@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}