package com.hydrateme.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hydrate Me — Home") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("Home Screen", style = MaterialTheme.typography.headlineMedium)

            Button(
                onClick = { navController.navigate("history") },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Go to History")
            }

            Button(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Go to Settings")
            }
        }
    }
}
