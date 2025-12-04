package com.hydrateme.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.hydrateme.app.data.dao.UserSettingsDao
import com.hydrateme.app.data.dao.WaterLogDao
import com.hydrateme.app.data.model.UserSettingsEntity
import com.hydrateme.app.data.model.WaterLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HydrateRepository(
    private val waterLogDao: WaterLogDao,
    private val userSettingsDao: UserSettingsDao
) {

    // --------------------------
    // WATER LOG FUNCTIONS
    // --------------------------

    // Add a new drink event
    suspend fun addWater(amount: Int) {
        val log = WaterLogEntity(
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        waterLogDao.insertLog(log)
    }

    //ResetToday's Logs
    suspend fun resetTodayLogs() {
        waterLogDao.deleteTodayLogs()
    }


    // Stream of today's logs
    fun getTodayLogs(): Flow<List<WaterLogEntity>> {
        return waterLogDao.getTodayLogs()
    }

    // Stream of last 7 days of logs
    fun getLast7DaysLogs(): Flow<List<WaterLogEntity>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return waterLogDao.getLast7Days(sevenDaysAgo)
    }

    // All logs (for history screen later if you want)
    fun getAllLogs(): Flow<List<WaterLogEntity>> {
        return waterLogDao.getAllLogs()
    }

    // --------------------------
    // USER SETTINGS FUNCTIONS
    // --------------------------

    fun getUserSettings(): Flow<UserSettingsEntity?> {
        return userSettingsDao.getSettings()
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        userSettingsDao.saveSettings(settings)
    }

    suspend fun ensureDefaultSettings() {
        val current = userSettingsDao.getSettingsOnce()
        if (current == null) {
            userSettingsDao.saveSettings(UserSettingsEntity())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDailyIntakeLast7Days(): Flow<Map<String, Int>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000

        return waterLogDao.getLast7Days(sevenDaysAgo).map { logs ->
            logs.groupBy { log ->
                // Format timestamp → yyyy-MM-dd string
                java.time.Instant.ofEpochMilli(log.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()
            }.mapValues { (_, dayLogs) ->
                dayLogs.sumOf { it.amount }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDailyIntakeLast30Days(): Flow<Map<String, Int>> {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000

        return waterLogDao.getAllLogs().map { logs ->
            logs.filter { it.timestamp >= thirtyDaysAgo }
                .groupBy { log ->
                    java.time.Instant.ofEpochMilli(log.timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .toString()
                }.mapValues { (_, dayLogs) ->
                    dayLogs.sumOf { it.amount }
                }
        }
    }

    suspend fun addWaterAtTime(amount: Int, timestamp: Long) {
        val log = WaterLogEntity(
            amount = amount,
            timestamp = timestamp
        )
        waterLogDao.insertLog(log)
    }


    suspend fun generateFakeData() {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000

        for (i in 1..30) {
            val dayTimestamp = now - (i * oneDay)

            val randomAmount = (40..120).random() // in oz or whatever units

            val log = WaterLogEntity(
                amount = randomAmount,
                timestamp = dayTimestamp
            )

            waterLogDao.insertLog(log)
        }
    }

}
