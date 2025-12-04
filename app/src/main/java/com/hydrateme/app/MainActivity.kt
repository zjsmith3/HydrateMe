package com.hydrateme.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.hydrateme.app.ui.navigation.AppNavHost
import com.hydrateme.app.ui.theme.HydrateMeTheme

class MainActivity : ComponentActivity() {

    // 🔹 Launcher that will show the system permission dialog
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            // You can show a Toast/log here later if you want
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If you have this helper, keep it:
        com.hydrateme.app.notifications.HydrationNotificationHelper
            .createChannel(this)

        // 🔹 Ask for notification permission on Android 13+ (API 33+)
        askNotificationPermissionIfNeeded()

        enableEdgeToEdge()
        setContent {
            HydrateMeTheme {
                AppNavHost()
            }
        }
    }

    // 🔹 Only runs on Android 13+; older versions skip this
    private fun askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestNotificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HydrateMeTheme {
        Greeting("Android")
    }
}
