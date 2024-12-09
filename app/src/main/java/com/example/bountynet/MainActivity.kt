package com.example.bountynet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bountynet.Objects.Bounty
import com.example.bountynet.pages.BountyDetailPage
import com.example.bountynet.pages.CreateBountyPage
import com.example.bountynet.pages.LoginScreen
import com.example.bountynet.ui.theme.BountyNetTheme
import com.google.firebase.FirebaseApp
import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            BountyNetTheme {
                AppNavigation() // Pass ViewModel down
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var userId = ""
    val navController = rememberNavController()
    val gson = Gson() // Used for serialization and deserialization

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { id ->
                    userId = id
                    navController.navigate("home")
                    println(id)
                }
            )
        }
        composable("home") {
            MainScreen(
                userId = userId,
                navController = navController,
            )
        }
        composable(
            "bountyDetail/{bounty}",
            arguments = listOf(navArgument("bounty") { type = NavType.StringType })
        ) { backStackEntry ->
            val bountyJson = backStackEntry.arguments?.getString("bounty")
            val bounty = gson.fromJson(bountyJson, Bounty::class.java) // Deserialize JSON to Bounty object

            BountyDetailPage(
                bounty = bounty
            )
        }
        composable("createBounty") {
            CreateBountyPage(
                navController = navController
            )
        }
    }
}


