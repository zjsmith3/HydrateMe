package com.hydrateme.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hydrateme.app.data.model.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    // Insert a drink entry
    @Insert
    suspend fun insertLog(log: WaterLogEntity)

    // Get ALL logs sorted by newest
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterLogEntity>>

    // Get ONLY today's logs
    @Query("""
        SELECT * FROM water_logs
        WHERE date(timestamp/1000, 'unixepoch') = date('now')
    """)
    fun getTodayLogs(): Flow<List<WaterLogEntity>>

    // ✅ NEW: delete ONLY today's logs
    @Query("""
        DELETE FROM water_logs
        WHERE date(timestamp/1000, 'unixepoch') = date('now')
    """)
    suspend fun deleteTodayLogs()

    // Get logs from last 7 days
    @Query("""
        SELECT * FROM water_logs
        WHERE timestamp >= :sevenDaysAgo
        ORDER BY timestamp DESC
    """)
    fun getLast7Days(sevenDaysAgo: Long): Flow<List<WaterLogEntity>>
}
