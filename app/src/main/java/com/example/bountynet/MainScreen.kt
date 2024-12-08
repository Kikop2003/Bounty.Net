package com.example.bountynet


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Face
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
import androidx.navigation.NavHostController
import com.example.bountynet.pages.Base
import com.example.bountynet.pages.BountyListPage
import com.example.bountynet.pages.Current
import com.example.bountynet.pages.Profile

@Composable
fun MainScreen(modifier: Modifier = Modifier,userViewModel: UserViewModel, navController: NavHostController) {

    val navItems = listOf(
        NavItem("List",Icons.Default.AllInbox),
        NavItem("Current",Icons.Default.PlayArrow, "meh"),
        NavItem("Profile",Icons.Default.Face, "mih")
    )
    val currentBackStackEntry = navController.currentBackStackEntry
    val savedIndex = currentBackStackEntry?.savedStateHandle?.get<Int>("selectedIndex")

    var selectedIndex by remember { mutableIntStateOf(savedIndex ?: -1) }

    Scaffold(modifier = Modifier.fillMaxSize(),
        bottomBar ={
            NavigationBar {
                navItems.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedIndex", index)
                        },
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
        ContentScreen(modifier = Modifier.padding(innerPadding),selectedIndex,navController)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier,selectedIndex : Int, navController: NavHostController) {
    when (selectedIndex) {
        -1 -> Base(modifier)
        0 -> BountyListPage(modifier,navController)
        1 -> Current(modifier)
        2 -> Profile(modifier)
    }
}