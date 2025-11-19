package com.hydrateme.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hydrateme.app.data.dao.UserSettingsDao
import com.hydrateme.app.data.dao.WaterLogDao
import com.hydrateme.app.data.model.UserSettingsEntity
import com.hydrateme.app.data.model.WaterLogEntity

@Database(
    entities = [
        WaterLogEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HydrateDatabase : RoomDatabase() {

    abstract fun waterLogDao(): WaterLogDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: HydrateDatabase? = null

        fun getDatabase(context: Context): HydrateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HydrateDatabase::class.java,
                    "hydrate_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
