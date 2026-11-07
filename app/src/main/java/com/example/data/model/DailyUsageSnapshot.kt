package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_usage_snapshots")
data class DailyUsageSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // e.g. "2026-10-04"
    val packageName: String,
    val appName: String,
    val isEntertainment: Boolean = false,
    val usedMinutes: Int = 0,
    val limitMinutes: Int = 0,
    val extraMinutesUsed: Int = 0,
    val violationsCount: Int = 0
)
