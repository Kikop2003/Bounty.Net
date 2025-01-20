package com.example.bountynet

import PhotoPage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.bountynet.ui.theme.bountyNetTheme
import com.google.common.reflect.TypeToken
import com.google.firebase.FirebaseApp
import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            bountyNetTheme(dynamicColor = false) {
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
        composable("photo"){
            PhotoPage(
                navController = navController,
                userId = userId
            )
        }
        composable(
            "bountyDetail/{pair}",
            arguments = listOf(navArgument("pair") { type = NavType.StringType })
        ) { backStackEntry ->
            val pairJson = backStackEntry.arguments?.getString("pair")
            val pairType = object : TypeToken<Pair<String, Bounty>>() {}.type
            val pair: Pair<String, Bounty> = gson.fromJson(pairJson, pairType)
            BountyDetailPage(
                pair = pair,
                navController = navController,
                userId = userId
            )
        }
        composable("createBounty") {
            CreateBountyPage(
                navController = navController,
                userId = userId
            )
        }
    }
}


