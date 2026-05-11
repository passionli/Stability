package com.example.stability.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stability.ui.auth.LoginScreen
import com.example.stability.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
    }
}
