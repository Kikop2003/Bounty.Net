package com.example.bountynet.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.bountynet.R

@Composable
fun Base(modifier: Modifier = Modifier){
    Text(text = "Base", modifier = modifier)
    Column {Image(painterResource(R.drawable.logo), contentDescription = null)}
}