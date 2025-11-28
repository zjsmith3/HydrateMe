package com.hydrateme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels              // NEW: for ViewModel delegation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hydrateme.app.data.AppDatabase        // NEW: our Room database
import com.hydrateme.app.data.repository.WaterRepository          // NEW
import com.hydrateme.app.data.repository.UserSettingsRepository   // NEW
import com.hydrateme.app.ui.theme.HydrateMeTheme
import com.hydrateme.app.viewmodel.WaterViewModel                  // NEW
import com.hydrateme.app.viewmodel.WaterViewModelFactory           // NEW
import com.hydrateme.app.viewmodel.UserSettingsViewModel           // NEW
import com.hydrateme.app.viewmodel.UserSettingsViewModelFactory    // NEW
import com.hydrateme.app.ui.home.HomeScreen   // NEW


class MainActivity : ComponentActivity() {

    // NEW: Single database instance
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    // NEW: Water logs repository
    private val waterRepository: WaterRepository by lazy {
        WaterRepository(database.waterLogDao())
    }

    // NEW: User settings repository
    private val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(database.userSettingsDao())
    }

    // NEW: ViewModel for water logs
    private val waterViewModel: WaterViewModel by viewModels {
        WaterViewModelFactory(waterRepository)
    }

    // NEW: ViewModel for user settings
    private val userSettingsViewModel: UserSettingsViewModel by viewModels {
        UserSettingsViewModelFactory(userSettingsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HydrateMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Call our HomeScreen and pass the WaterViewModel + padding.
                    HomeScreen(
                        waterViewModel = waterViewModel,
                        innerPadding = innerPadding
                    )
                }

            }
        }
    }
}

// Simple composable we can see on the screen for now
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    com.hydrateme.app.ui.theme.HydrateMeTheme {
        Greeting("Android")
    }
}
