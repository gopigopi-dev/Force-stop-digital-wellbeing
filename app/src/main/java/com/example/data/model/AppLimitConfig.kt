package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimitConfig(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isRestricted: Boolean = false,
    val isEntertainment: Boolean = false,
    val dailyLimitMinutes: Int = 30, // 0 means unconstrained
    val extraTimeMinutesGrantedToday: Int = 0,
    val lastResetDate: String = "",
    val category: String = "General"
)
