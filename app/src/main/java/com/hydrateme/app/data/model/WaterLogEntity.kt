package com.hydrateme.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Each row = one time the user drank water
@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // amount of water logged (pick ml or oz for your app & stay consistent)
    val amount: Int,

    // when this drink was logged (milliseconds since 1970)
    val timestamp: Long = System.currentTimeMillis()
)