package com.hydrateme.app.data

//Imports for the Room
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//DAOs and Entitles
import com.hydrateme.app.data.dao.UserSettingsDao
import com.hydrateme.app.data.dao.WaterLogDao
import com.hydrateme.app.data.model.UserSettingsEntity
import com.hydrateme.app.data.model.WaterLogEntity

// @Database tells Room:
// - which entity classes become tables
// - the schema version of the database
@Database(
    entities = [
        UserSettingsEntity::class,   // ← note the ::class
        WaterLogEntity::class        // ← note the ::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun waterLogDao(): WaterLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hydrate_me_db"
                )
                    // You can ignore this deprecation warning for class work,
                    // or change to .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
