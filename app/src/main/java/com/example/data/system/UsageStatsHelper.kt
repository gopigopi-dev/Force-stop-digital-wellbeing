package com.example.data.system

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.util.Calendar

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean
)

object UsageStatsHelper {

    val KNOWN_ENTERTAINMENT_PACKAGES = setOf(
        "com.google.android.youtube",
        "com.instagram.android",
        "com.netflix.mediaclient",
        "com.facebook.katana",
        "com.snapchat.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.twitter.android",
        "tv.twitch.android.app",
        "com.reddit.frontpage",
        "com.spotify.music",
        "com.disney.disneyplus",
        "com.hulu.plus",
        "com.valvesoftware.android.steam.community",
        "com.supercell.clashofclans",
        "com.roblox.client",
        "com.king.candycrushsaga"
    )

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    fun openOverlaySettings(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    fun getCycleStartTime(resetHour: Int, resetMinute: Int): Long {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        if (currentHour < resetHour || (currentHour == resetHour && currentMinute < resetMinute)) {
            // Cycle started yesterday at resetHour:resetMinute
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, resetHour)
        calendar.set(Calendar.MINUTE, resetMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun queryTodayUsage(context: Context, resetHour: Int = 0, resetMinute: Int = 0): Map<String, Long> {
        if (!hasUsageStatsPermission(context)) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val startTime = getCycleStartTime(resetHour, resetMinute)
        val endTime = System.currentTimeMillis()

        return try {
            val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            val result = mutableMapOf<String, Long>()
            statsMap.forEach { (pkg, stats) ->
                if (stats.totalTimeInForeground > 0) {
                    result[pkg] = stats.totalTimeInForeground
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun queryCurrentForegroundApp(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 15000 // Last 15 seconds

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundPackage: String? = null
        var lastEventTime: Long = 0

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val eventType = event.eventType
            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                eventType == 1 /* UsageEvents.Event.MOVE_TO_FOREGROUND */
            ) {
                if (event.timeStamp > lastEventTime) {
                    lastEventTime = event.timeStamp
                    lastForegroundPackage = event.packageName
                }
            }
        }
        return lastForegroundPackage
    }

    fun getInstalledApps(context: Context): List<InstalledAppItem> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val seen = mutableSetOf<String>()
        val result = mutableListOf<InstalledAppItem>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) continue // Exclude self
            if (!seen.add(pkg)) continue

            val appName = info.loadLabel(pm).toString()
            val icon = try {
                info.loadIcon(pm)
            } catch (_: Exception) {
                null
            }
            val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            result.add(
                InstalledAppItem(
                    packageName = pkg,
                    appName = appName,
                    icon = icon,
                    isSystemApp = isSystem
                )
            )
        }

        // If running in an emulator with minimal launcher apps, ensure representative popular apps are present
        val fallbackApps = listOf(
            Triple("com.google.android.youtube", "YouTube", true),
            Triple("com.instagram.android", "Instagram", true),
            Triple("com.netflix.mediaclient", "Netflix", true),
            Triple("com.zhiliaoapp.musically", "TikTok", true),
            Triple("com.facebook.katana", "Facebook", true),
            Triple("com.twitter.android", "X (Twitter)", true),
            Triple("com.reddit.frontpage", "Reddit", true),
            Triple("com.spotify.music", "Spotify", true),
            Triple("com.google.android.apps.messaging", "Messages", false),
            Triple("com.google.android.gm", "Gmail", false),
            Triple("com.android.chrome", "Chrome", false)
        )

        for ((pkg, name, _) in fallbackApps) {
            if (pkg != context.packageName && seen.add(pkg)) {
                val icon = try {
                    pm.getApplicationIcon(pkg)
                } catch (_: Exception) {
                    null
                }
                result.add(
                    InstalledAppItem(
                        packageName = pkg,
                        appName = name,
                        icon = icon,
                        isSystemApp = false
                    )
                )
            }
        }

        return result.sortedBy { it.appName.lowercase() }
    }
}
