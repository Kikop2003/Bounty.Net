package com.example.bountynet.pages

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bountynet.Bounty

@Composable
fun BountyDetailPage(modifier: Modifier = Modifier, bounty: Bounty) {
    Text(text = "Bounty Name: ${bounty.name}")
}