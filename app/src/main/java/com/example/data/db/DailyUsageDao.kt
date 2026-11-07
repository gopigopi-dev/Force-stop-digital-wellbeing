package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyUsageSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {
    @Query("SELECT * FROM daily_usage_snapshots WHERE dateString = :dateStr ORDER BY usedMinutes DESC")
    fun getSnapshotsForDate(dateStr: String): Flow<List<DailyUsageSnapshot>>

    @Query("SELECT * FROM daily_usage_snapshots WHERE packageName = :packageName ORDER BY dateString DESC LIMIT 14")
    fun getSnapshotsForPackage(packageName: String): Flow<List<DailyUsageSnapshot>>

    @Query("SELECT * FROM daily_usage_snapshots ORDER BY dateString DESC, usedMinutes DESC")
    fun getAllSnapshots(): Flow<List<DailyUsageSnapshot>>

    @Query("SELECT DISTINCT dateString FROM daily_usage_snapshots ORDER BY dateString DESC LIMIT 7")
    fun getRecentDates(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: DailyUsageSnapshot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<DailyUsageSnapshot>)

    @Query("UPDATE daily_usage_snapshots SET violationsCount = violationsCount + 1 WHERE packageName = :packageName AND dateString = :dateStr")
    suspend fun incrementViolation(packageName: String, dateStr: String)
}
