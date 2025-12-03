package com.hydrateme.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hydrateme.app.data.database.HydrateDatabase
import com.hydrateme.app.data.repository.HydrateRepository
import com.hydrateme.app.viewmodel.HydrateViewModel
import com.hydrateme.app.viewmodel.HydrateViewModelFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.hydrateme.app.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    // Build ViewModel
    val context = LocalContext.current
    val db = HydrateDatabase.getDatabase(context)
    val repository = HydrateRepository(db.waterLogDao(), db.userSettingsDao())
    val viewModel: HydrateViewModel = viewModel(
        factory = HydrateViewModelFactory(repository)
    )

    // Observe today's logs
    val todayLogs by viewModel.todayLogs.observeAsState(emptyList())

    // Observe settings
    val settings = viewModel.userSettings.observeAsState().value

    // If settings aren't loaded yet, show loading
    if (settings == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val units = settings.units
    val goal = settings.dailyGoal
    val totalToday = todayLogs.sumOf { it.amount }

    val progress = if (goal == 0) 0f else (totalToday.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hydrate Me") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ------------------------------------------
            // Today's Progress Section
            // ------------------------------------------
            Text(
                text = "Today's Water Intake",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "$totalToday / $goal $units",
                style = MaterialTheme.typography.headlineMedium
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )

            // ------------------------------------------
            // Quick Add Buttons
            // ------------------------------------------
            Text(
                text = "Quick Add",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickAddButton("+8 $units") { viewModel.addWater(8) }
                QuickAddButton("+12 $units") { viewModel.addWater(12) }
                QuickAddButton("+16 $units") { viewModel.addWater(16) }
            }

            // ------------------------------------------
            // Navigation Buttons
            // ------------------------------------------
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("history") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View History")
            }

            Button(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Settings")
            }
            // Push logo to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // HydrateMe logo at the bottom center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hydrateme_logo),
                    contentDescription = "HydrateMe Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.55f)   // ~55% of screen width so it fits nicely
                        .aspectRatio(1f)       // keep proportions, no stretching
                )
            }

        }
    }
}

@Composable
fun QuickAddButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}
