package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AppLimitConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limits ORDER BY appName ASC")
    fun getAllLimits(): Flow<List<AppLimitConfig>>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName")
    fun getLimitForPackage(packageName: String): Flow<AppLimitConfig?>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName")
    suspend fun getLimitForPackageSync(packageName: String): AppLimitConfig?

    @Query("SELECT * FROM app_limits WHERE isRestricted = 1")
    fun getRestrictedLimits(): Flow<List<AppLimitConfig>>

    @Query("SELECT * FROM app_limits WHERE isEntertainment = 1")
    fun getEntertainmentLimits(): Flow<List<AppLimitConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLimit(config: AppLimitConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLimits(configs: List<AppLimitConfig>)

    @Query("UPDATE app_limits SET extraTimeMinutesGrantedToday = extraTimeMinutesGrantedToday + :additionalMinutes WHERE packageName = :packageName")
    suspend fun addExtraTime(packageName: String, additionalMinutes: Int)

    @Query("UPDATE app_limits SET extraTimeMinutesGrantedToday = 0, lastResetDate = :todayDate")
    suspend fun resetAllDailyExtraTime(todayDate: String)

    @Query("DELETE FROM app_limits WHERE packageName = :packageName")
    suspend fun deleteLimit(packageName: String)
}
