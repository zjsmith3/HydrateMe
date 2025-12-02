package com.hydrateme.app.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hydration History") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "History Screen",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Back to Home")
            }
        }
    }
}
