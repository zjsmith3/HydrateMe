package com.hydrateme.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Single row that holds user preferences & stats
@Entity(tableName = "user_settings")
data class UserSettingsEntity(

    // We always have just ONE row, so we hardcode id = 1
    @PrimaryKey
    val id: Int = 1,

    // --- GOAL & UNITS ---
    // daily target in your chosen unit (e.g., ml or oz)
    val dailyGoal: Int = 2000,
    val units: String = "oz",   // or "oz"

    // --- REMINDERS ---
    val remindersEnabled: Boolean = true,
    val reminderIntervalHours: Int = 2,

    // --- ACHIEVEMENTS / STATS ---
    val achievementLevel: Int = 0,
    val totalIntakeAllTime: Int = 0
)
