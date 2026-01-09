package com.example.smartparking.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartparking.ui.screen.MapScreen
import com.example.smartparking.ui.screen.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Map : Screen("map")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen()
        }
    }
}
