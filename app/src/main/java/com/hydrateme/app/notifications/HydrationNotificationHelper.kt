package com.hydrateme.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hydrateme.app.R

object HydrationNotificationHelper {

    const val CHANNEL_ID = "hydrate_reminders_channel"
    private const val CHANNEL_NAME = "Hydration Reminders"
    private const val CHANNEL_DESC = "Shows reminders to drink water."

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH   // 🔹 was DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showHydrationReminder(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to hydrate 💧")
            .setContentText("Take a quick sip of water and stay on track!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)          // 🔹 heads-up
            .setDefaults(NotificationCompat.DEFAULT_ALL)            // 🔹 sound/vibrate if allowed
            .setAutoCancel(true)
            .build()

        // 🔹 Fixes the "Call requires permission" warning
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(context)) {
                notify(1001, notification)
            }
        }
    }
}
