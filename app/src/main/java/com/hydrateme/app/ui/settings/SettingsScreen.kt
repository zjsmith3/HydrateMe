package com.hydrateme.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

import com.hydrateme.app.data.database.HydrateDatabase
import com.hydrateme.app.data.model.UserSettingsEntity
import com.hydrateme.app.data.repository.HydrateRepository
import com.hydrateme.app.viewmodel.HydrateViewModel
import com.hydrateme.app.viewmodel.HydrateViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {

    // ViewModel setup
    val context = LocalContext.current
    val db = HydrateDatabase.getDatabase(context)
    val repository = HydrateRepository(db.waterLogDao(), db.userSettingsDao())
    val viewModel: HydrateViewModel = viewModel(factory = HydrateViewModelFactory(repository))

    // Observe data from DB (may be null!)
    val settings = viewModel.userSettings.observeAsState().value

    // Wait for DB to load before building UI
    if (settings == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Local state (mutable)
    var dailyGoal by remember { mutableStateOf(settings.dailyGoal.toString()) }
    var units by remember { mutableStateOf(settings.units) }
    var remindersEnabled by remember { mutableStateOf(settings.remindersEnabled) }
    var reminderInterval by remember { mutableStateOf(settings.reminderIntervalHours.toString()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            OutlinedTextField(
                value = dailyGoal,
                onValueChange = { dailyGoal = it },
                label = { Text("Daily Goal (${units})") },
                modifier = Modifier.fillMaxWidth()
            )

            // Units dropdown
            var unitsExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = unitsExpanded,
                onExpandedChange = { unitsExpanded = !unitsExpanded }
            ) {
                OutlinedTextField(
                    value = units,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Units") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = unitsExpanded,
                    onDismissRequest = { unitsExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("oz") },
                        onClick = {
                            units = "oz"
                            unitsExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("ml") },
                        onClick = {
                            units = "ml"
                            unitsExpanded = false
                        }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Reminders")
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { remindersEnabled = it }
                )
            }

            if (remindersEnabled) {
                var intervalExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = intervalExpanded,
                    onExpandedChange = { intervalExpanded = !intervalExpanded }
                ) {
                    OutlinedTextField(
                        value = reminderInterval,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reminder Interval (hours)") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = intervalExpanded,
                        onDismissRequest = { intervalExpanded = false }
                    ) {
                        (1..6).forEach { hour ->
                            DropdownMenuItem(
                                text = { Text("$hour") },
                                onClick = {
                                    reminderInterval = hour.toString()
                                    intervalExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val updated = settings.copy(
                        dailyGoal = dailyGoal.toIntOrNull() ?: settings.dailyGoal,
                        units = units,
                        remindersEnabled = remindersEnabled,
                        reminderIntervalHours = reminderInterval.toIntOrNull()
                            ?: settings.reminderIntervalHours
                    )

                    viewModel.saveUserSettings(updated)

                    // Start or stop reminders based on new settings
                    if (updated.remindersEnabled) {
                        HydrationReminderScheduler.scheduleHydrationReminders(
                            context = context,
                            intervalHours = updated.reminderIntervalHours.toLong()
                        )
                    } else {
                        HydrationReminderScheduler.cancelHydrationReminders(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))

// --------------------
//  ADD FAKE DATA BUTTON
// --------------------
            Button(
                onClick = {
                    viewModel.generateFakeData()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF448AFF) // Blue-ish makes it stand out
                )
            ) {
                Text("Generate Fake Data (30 Days)")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 NEW: Test notification button
            Button(
                onClick = {
                    HydrationReminderScheduler.scheduleTestReminder(context)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // green-ish for "test"
                    contentColor = Color.White
                )
            ) {
                Text("Test Notification (5s)")
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}
