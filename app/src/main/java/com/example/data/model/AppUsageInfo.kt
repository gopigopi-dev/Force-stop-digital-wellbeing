package com.example.data.model

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val category: String = "General",
    val isRestricted: Boolean = false,
    val isEntertainment: Boolean = false,
    val dailyLimitMinutes: Int = 0, // 0 = no limit
    val usedTodayMinutes: Int = 0,
    val extraTimeMinutesGrantedToday: Int = 0,
    val todayViolationsCount: Int = 0
) {
    val totalAllowedMinutes: Int
        get() = if (dailyLimitMinutes > 0) dailyLimitMinutes + extraTimeMinutesGrantedToday else 0

    val remainingMinutes: Int
        get() = if (totalAllowedMinutes > 0) {
            (totalAllowedMinutes - usedTodayMinutes).coerceAtLeast(0)
        } else {
            -1 // No limit
        }

    val isLimitReached: Boolean
        get() = isRestricted && totalAllowedMinutes > 0 && usedTodayMinutes >= totalAllowedMinutes

    val isWarningState: Boolean
        get() = isRestricted && totalAllowedMinutes > 0 && !isLimitReached && remainingMinutes in 1..10

    val usagePercentage: Float
        get() = if (totalAllowedMinutes > 0) {
            (usedTodayMinutes.toFloat() / totalAllowedMinutes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    fun formattedUsedTime(): String {
        val hours = usedTodayMinutes / 60
        val mins = usedTodayMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    fun formattedLimitTime(): String {
        if (dailyLimitMinutes <= 0) return "No limit"
        val hours = dailyLimitMinutes / 60
        val mins = dailyLimitMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m/day"
            hours > 0 -> "${hours}h/day"
            else -> "${mins}m/day"
        }
    }

    fun formattedRemainingTime(): String {
        if (totalAllowedMinutes <= 0) return "Unlimited"
        if (isLimitReached) return "Limit reached"
        val hours = remainingMinutes / 60
        val mins = remainingMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m left"
            hours > 0 -> "${hours}h left"
            else -> "${mins}m left"
        }
    }
}
