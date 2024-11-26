package com.example.bountynet


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.bountynet.pages.Base
import com.example.bountynet.pages.Current
import com.example.bountynet.pages.HomePage
import com.example.bountynet.pages.Profile

@Composable
fun MainScreen(modifier: Modifier = Modifier) {

    val navItems = listOf(
        NavItem("Home",Icons.Default.Home),
        NavItem("Current",Icons.Default.PlayArrow, "meh"),
        NavItem("Profile",Icons.Default.Face, "mih")
    )
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Scaffold(modifier = Modifier.fillMaxSize(),
        bottomBar ={
            NavigationBar {
                navItems.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon =  {
                            BadgedBox(badge = {
                                if (navItem.badgeText != null){
                                    Badge() {
                                        Text(text = navItem.badgeText.toString())
                                    }
                                }
                            }){
                                Icon(imageVector = navItem.icon, contentDescription = "Icon")
                            }
                        },
                        label = { Text(text = navItem.label) },
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding),selectedIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier,selectedIndex : Int) {
    when (selectedIndex) {
        -1 -> Base(modifier)
        0 -> HomePage(modifier)
        1 -> Current(modifier)
        2 -> Profile(modifier)
    }
}