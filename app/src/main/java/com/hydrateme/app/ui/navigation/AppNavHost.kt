package com.hydrateme.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hydrateme.app.ui.home.HomeScreen
import com.hydrateme.app.ui.history.HistoryScreen
import com.hydrateme.app.ui.settings.SettingsScreen

@Composable
fun AppNavHost() {

    // Creates the navigation controller
    val navController: NavHostController = rememberNavController()

    // Defines all screens + start screen
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }
        composable("history") {
            HistoryScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
    }
}
