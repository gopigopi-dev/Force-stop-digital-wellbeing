package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppLimitConfig
import com.example.data.model.AppSettingEntity
import com.example.data.model.DailyUsageSnapshot

@Database(
    entities = [
        AppLimitConfig::class,
        DailyUsageSnapshot::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ForceStopDatabase : RoomDatabase() {
    abstract fun appLimitDao(): AppLimitDao
    abstract fun dailyUsageDao(): DailyUsageDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: ForceStopDatabase? = null

        fun getDatabase(context: Context): ForceStopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ForceStopDatabase::class.java,
                    "force_stop_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
