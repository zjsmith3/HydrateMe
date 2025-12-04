package com.hydrateme.app.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object HydrationReminderScheduler {

    private const val UNIQUE_WORK_NAME = "hydration_reminder_work"

    // Normal repeating reminder (every X hours)
    fun scheduleHydrationReminders(
        context: Context,
        intervalHours: Long
    ) {
        val workManager = WorkManager.getInstance(context.applicationContext)

        val constraints = Constraints.Builder()
            .build()

        val request = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            intervalHours,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelHydrationReminders(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    // 🔹 NEW: one-time test reminder after 5 seconds
    fun scheduleTestReminder(context: Context) {
        val constraints = Constraints.Builder()
            .build()

        val testRequest = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueue(testRequest)
    }
}
