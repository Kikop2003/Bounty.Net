package com.example.bountynet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bountynet.pages.LoginScreen
import com.example.bountynet.ui.theme.BountyNetTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val userViewModel by viewModels<UserViewModel>() // Create ViewModel instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            BountyNetTheme {
                AppNavigation(userViewModel) // Pass ViewModel down
            }
        }
    }
}

@Composable
fun AppNavigation(userViewModel: UserViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { username ->
                    userViewModel.logout()
                    userViewModel.setUsername(username) // Save username in ViewModel
                    navController.navigate("home")
                }
            )
        }
        composable("home") {
            MainScreen(
                userViewModel = userViewModel
            )
        }
    }
}


